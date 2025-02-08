package com.example.digital_signature.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.digital_signature.model.DocumentModel;
import com.example.digital_signature.model.UserModel;
import com.example.digital_signature.repository.DocumentRepo;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

@Service
public class DocumentService {
    @Value("D:/Spring Boot/digital-signature/data")
    private String folderPath;

    @Autowired
    private DocumentRepo documentRepo;

    private String hashFileContent(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(content);
        StringBuilder hexString = new StringBuilder();

        for (byte i : hashBytes) {
            String hex = Integer.toHexString(0xff & i);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String encodeFileHash(String fileHash, UserModel user) throws Exception {
        byte[] privateKeyByte = Base64.getDecoder().decode(user.getPrivateKey());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyByte));

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(fileHash.getBytes());

        byte[] signaturebyte = signature.sign();
        return Base64.getEncoder().encodeToString(signaturebyte);
    }

    public String saveFile(MultipartFile file, UserModel user) throws Exception {
        if (file.isEmpty()) {
            return "File is empty, cannot upload";
        }

        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(folderPath, fileName);

        Files.write(filePath, file.getBytes());

        String fileHash = hashFileContent(file.getBytes());
        String encodeHashFile = encodeFileHash(fileHash, user);

        DocumentModel newFile = new DocumentModel();
        newFile.setDocumentName(fileName);
        newFile.setDocumentPath(filePath.toString());
        newFile.setUser(user);
        newFile.setHashfile(fileHash);
        newFile.setDocumentHash(encodeHashFile);
        newFile.setSignedBy("");

        documentRepo.save(newFile);

        return "File uploaded successfully";
    }

    public boolean verifyFileHash(DocumentModel document, UserModel user) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(user.getPublicKey());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Path filePath = Paths.get(document.getDocumentPath());
        byte[] fileBytes = Files.readAllBytes(filePath);

        String fileHash = hashFileContent(fileBytes);

        byte[] signatureBytes = Base64.getDecoder().decode(document.getDocumentHash());

        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(fileHash.getBytes());

        return signature.verify(signatureBytes);
    }

    public String signDocument(int documentID, UserModel user, String signature) throws Exception {
        try {
            DocumentModel document = documentRepo.findByID(documentID);
            String signed_by = document.getSignedBy();
            String newSign = signed_by + user.getID() + "/";

            byte[] signatureImageBytes = Base64.getDecoder().decode(signature);
            PdfReader pdfReader = new PdfReader(document.getDocumentPath());

            String newName = document.getDocumentName().replace(".pdf", "_") + user.getID() + ".pdf";
            String newPath = document.getDocumentPath().replace(document.getDocumentName(), newName);

            File tempFile = new File(newPath);
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            PdfStamper pdfStamper = new PdfStamper(pdfReader, outputStream);

            Image signatureImage = Image.getInstance(signatureImageBytes);
            signatureImage.scaleToFit(200, 100);

            String userID = String.valueOf(user.getID()); // Chuyển userID sang String để so sánh

            for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
                PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
                TextExtractionStrategy strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                String text = strategy.getResultantText();

                Pattern pattern = Pattern.compile("\\$<\\s*(\\d+)");
                Matcher matcher = pattern.matcher(text);

                while (matcher.find()) {
                    String number = matcher.group(1); // Lấy số từ $<number>

                    // 🔥 Kiểm tra nếu số này trùng với userID thì mới tiếp tục
                    if (number.equals(userID)) {
                        float[] position = getTextPosition(pdfReader, i, "$<" + number);
                        if (position != null) {
                            float x = position[0];
                            float y = position[1];

                            PdfContentByte content = pdfStamper.getOverContent(i);

                            content.setColorFill(BaseColor.WHITE);
                            content.rectangle(x, y, 50, 10);
                            content.fill();

                            signatureImage.setAbsolutePosition(x - 70, y - 50);
                            content.addImage(signatureImage);
                        } else {
                            throw new RuntimeException("Cannot find the place to sign for user " + userID);
                        }
                    }
                }
            }

            pdfStamper.close();
            outputStream.close();
            pdfReader.close();

            byte[] newFileBytes = Files.readAllBytes(tempFile.toPath());
            String newFileHash = hashFileContent(newFileBytes);

            String encodedHashFile = encodeFileHash(newFileHash, user);

            document.setSignedBy(newSign);
            document.setDocumentPath(newPath);
            document.setHashfile(newFileHash);
            document.setDocumentHash(encodedHashFile);

            documentRepo.save(document);

            return "Sign Success";
        } catch (Exception e) {
            throw new RuntimeException("Error while signing the document: " + e.getMessage());
        }
    }

    private float[] getTextPosition(PdfReader reader, int page, String searchText) throws IOException {
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        final float[] position = new float[2];

        final List<TextRenderInfo> textRenderInfoList = new ArrayList<>();

        parser.processContent(page, new RenderListener() {
            public void beginTextBlock() {
            }

            public void renderText(TextRenderInfo renderInfo) {
                textRenderInfoList.add(renderInfo);
            }

            public void endTextBlock() {
            }

            public void renderImage(ImageRenderInfo renderInfo) {
            }
        });

        StringBuilder fullText = new StringBuilder();
        for (TextRenderInfo info : textRenderInfoList) {
            fullText.append(info.getText());
        }

        int index = fullText.indexOf(searchText);
        if (index != -1) {
            int charCount = 0;
            for (TextRenderInfo info : textRenderInfoList) {
                String text = info.getText();

                if (charCount + text.length() > index) {
                    int relativePos = index - charCount;
                    if (relativePos >= 0 && relativePos < text.length()) {
                        Vector bottomLeft = info.getBaseline().getStartPoint();
                        position[0] = bottomLeft.get(0);
                        position[1] = bottomLeft.get(1);
                        break;
                    }
                }
                charCount += text.length();
            }
        } else {
            System.out.println("Cannot found " + searchText + " in " + page);
        }

        return (position[0] != 0 && position[1] != 0) ? position : null;
    }

}
