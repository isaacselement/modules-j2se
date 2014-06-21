package com.modules.Encryptor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class RSAEncryptor {
    
    
    // ___________________________________________  A Beautiful Separate Line  ___________________________________________
    
    
    // openssl version -a
    
    /**
     * 
     * 1. Create the RSA Private Key
     * 
     * openssl genrsa -out private_key.pem 1024                                                                         -> Generate 'private_key.pem'
     * 
     * 
     * 2. Create a certificate signing request with the private key
     * (This step u will enter some Info. For further reference, you'd better write them down or make a screen shot)
     * 
     * openssl req -new -key private_key.pem -out rsaCertReq.csr                                                        -> Generate 'rsaCertReq.csr'
     * 
     * 
     * 3. Create a self-signed certificate with the private key and signing request                                     
     * 
     * openssl x509 -req -days 3650 -in rsaCertReq.csr -signkey private_key.pem -out rsaCert.crt                        -> Generate 'rsaCert.crt'       
     * 
     * 
     * 
     * 
     * 4. Convert the certificate to DER format: the certificate contains the public key
     * 
     * openssl x509 -outform der -in rsaCert.crt -out public_key.der                                                    -> Generate 'public_key.der' (for IOS to encrypt) 
     * 
     * 
     * 5. Export the private key and certificate to p12 file. 
     * (This step will ask u to enter password, e.g 'ISAACS' for test, it will be used in your IOS Code, do not forget it)
     * 
     * openssl pkcs12 -export -out private_key.p12 -inkey private_key.pem -in rsaCert.crt                               -> Generate 'private_key.p12' (for IOS to decrypt) 
     * 
     * 
     * 
     * 
     * 6.
     * openssl rsa -in private_key.pem -out rsa_public_key.pem -pubout                                                  -> Generate 'rsa_public_key.pem' (for JAVA to encrypt)
     * 
     * 7.
     * openssl pkcs8 -topk8 -in private_key.pem -out pkcs8_private_key.pem -nocrypt                                     -> Generate 'pkcs8_private_key.pem' (for JAVA to decrypt)
     * 
     * 
     */
    
    /**
     * @param publicKeyFilePath     The file from step 4.
     * @param privateKeyFilePath    The file from step 5. PKCS#8 format private key file .
     */
    public RSAEncryptor(String publicKeyFilePath, String privateKeyFilePath) throws Exception {
        String public_key = getKeyFromFile(publicKeyFilePath);
        String private_key = getKeyFromFile(privateKeyFilePath);
        loadPublicKey(public_key);  
        loadPrivateKey(private_key);  
    }
    public RSAEncryptor() {
        // load the PublicKey and PrivateKey manually
    }
    
    
    public String getKeyFromFile(String filePath) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        
        String line = null;
        List<String> list = new ArrayList<String>();
        while ((line = bufferedReader.readLine()) != null){
            list.add(line);
        }
        
        // remove the firt line and last line
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i < list.size() - 1; i++) {
            stringBuilder.append(list.get(i)).append("\r");
        }
        
        String key = stringBuilder.toString();
        return key;
    }
    
    public String decryptWithBase64(String base64String) throws Exception {
        //  http://commons.apache.org/proper/commons-codec/ : org.apache.commons.codec.binary.Base64
        // sun.misc.BASE64Decoder
        byte[] binaryData = decrypt(getPrivateKey(), new BASE64Decoder().decodeBuffer(base64String) /*org.apache.commons.codec.binary.Base64.decodeBase64(base46String.getBytes())*/);
        String string = new String(binaryData);
        return string;
    }
    
    public String encryptWithBase64(String string) throws Exception {
        //  http://commons.apache.org/proper/commons-codec/ : org.apache.commons.codec.binary.Base64
        // sun.misc.BASE64Encoder
        byte[] binaryData = encrypt(getPublicKey(), string.getBytes());
        String base64String = new BASE64Encoder().encodeBuffer(binaryData) /* org.apache.commons.codec.binary.Base64.encodeBase64(binaryData) */;
        return base64String;
    }
  
    
    
    // convenient properties
    public static RSAEncryptor sharedInstance = null;
    public static void setSharedInstance (RSAEncryptor rsaEncryptor) {
        sharedInstance = rsaEncryptor;
    }
    
    
    // ___________________________________________  A Beautiful Separate Line  ___________________________________________
    
    
    
    
    
    
    
    
    // From: http://blog.csdn.net/chaijunkun/article/details/7275632

    /** 
     * 私钥 
     */  
    private RSAPrivateKey privateKey;  
  
    /** 
     * 公钥 
     */  
    private RSAPublicKey publicKey;  
      
    /** 
     * 获取私钥 
     * @return 当前的私钥对象 
     */  
    public RSAPrivateKey getPrivateKey() {  
        return privateKey;  
    }  
  
    /** 
     * 获取公钥 
     * @return 当前的公钥对象 
     */  
    public RSAPublicKey getPublicKey() {  
        return publicKey;  
    }  
  
    /** 
     * 随机生成密钥对 
     */  
    public void genKeyPair(){  
        KeyPairGenerator keyPairGen= null;  
        try {  
            keyPairGen= KeyPairGenerator.getInstance("RSA");  
        } catch (NoSuchAlgorithmException e) {  
            e.printStackTrace();  
        }  
        keyPairGen.initialize(1024, new SecureRandom());  
        KeyPair keyPair= keyPairGen.generateKeyPair();  
        this.privateKey= (RSAPrivateKey) keyPair.getPrivate();  
        this.publicKey= (RSAPublicKey) keyPair.getPublic();  
    }  
  
    /** 
     * 从文件中输入流中加载公钥 
     * @param in 公钥输入流 
     * @throws Exception 加载公钥时产生的异常 
     */  
    public void loadPublicKey(InputStream in) throws Exception{  
        try {  
            BufferedReader br= new BufferedReader(new InputStreamReader(in));  
            String readLine= null;  
            StringBuilder sb= new StringBuilder();  
            while((readLine= br.readLine())!=null){  
                if(readLine.charAt(0)=='-'){  
                    continue;  
                }else{  
                    sb.append(readLine);  
                    sb.append('\r');  
                }  
            }  
            loadPublicKey(sb.toString());  
        } catch (IOException e) {  
            throw new Exception("公钥数据流读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("公钥输入流为空");  
        }  
    }  
  
    /** 
     * 从字符串中加载公钥 
     * @param publicKeyStr 公钥数据字符串 
     * @throws Exception 加载公钥时产生的异常 
     */  
    public void loadPublicKey(String publicKeyStr) throws Exception{  
        try {  
            BASE64Decoder base64Decoder= new BASE64Decoder();  
            byte[] buffer= base64Decoder.decodeBuffer(publicKeyStr);
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");  
            X509EncodedKeySpec keySpec= new X509EncodedKeySpec(buffer);  
            this.publicKey= (RSAPublicKey) keyFactory.generatePublic(keySpec);  
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此算法");  
        } catch (InvalidKeySpecException e) {  
            throw new Exception("公钥非法");  
        } catch (IOException e) {  
            throw new Exception("公钥数据内容读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("公钥数据为空");  
        }  
    }  
  
    /** 
     * 从文件中加载私钥 
     * @param keyFileName 私钥文件名 
     * @return 是否成功 
     * @throws Exception  
     */  
    public void loadPrivateKey(InputStream in) throws Exception{  
        try {  
            BufferedReader br= new BufferedReader(new InputStreamReader(in));  
            String readLine= null;  
            StringBuilder sb= new StringBuilder();  
            while((readLine= br.readLine())!=null){  
                if(readLine.charAt(0)=='-'){  
                    continue;  
                }else{  
                    sb.append(readLine);  
                    sb.append('\r');  
                }  
            }  
            loadPrivateKey(sb.toString());  
        } catch (IOException e) {  
            throw new Exception("私钥数据读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("私钥输入流为空");  
        }  
    }  
  
    public void loadPrivateKey(String privateKeyStr) throws Exception{  
        try {  
            BASE64Decoder base64Decoder= new BASE64Decoder();  
            byte[] buffer= base64Decoder.decodeBuffer(privateKeyStr);  
            PKCS8EncodedKeySpec keySpec= new PKCS8EncodedKeySpec(buffer);  
            KeyFactory keyFactory= KeyFactory.getInstance("RSA");  
            this.privateKey= (RSAPrivateKey) keyFactory.generatePrivate(keySpec);  
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此算法");  
        } catch (InvalidKeySpecException e) {  
        	e.printStackTrace();
            throw new Exception("私钥非法");  
        } catch (IOException e) {  
            throw new Exception("私钥数据内容读取错误");  
        } catch (NullPointerException e) {  
            throw new Exception("私钥数据为空");  
        }  
    }  
  
    /** 
     * 加密过程 
     * @param publicKey 公钥 
     * @param plainTextData 明文数据 
     * @return 
     * @throws Exception 加密过程中的异常信息 
     */  
    public byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData) throws Exception{  
        if(publicKey== null){  
            throw new Exception("加密公钥为空, 请设置");  
        }  
        Cipher cipher= null;  
        try {  
            cipher= Cipher.getInstance("RSA");//, new BouncyCastleProvider());  
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);  
            byte[] output= cipher.doFinal(plainTextData);  
            return output;  
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此加密算法");  
        } catch (NoSuchPaddingException e) {  
            e.printStackTrace();  
            return null;  
        }catch (InvalidKeyException e) {  
            throw new Exception("加密公钥非法,请检查");  
        } catch (IllegalBlockSizeException e) {  
            throw new Exception("明文长度非法");  
        } catch (BadPaddingException e) {  
            throw new Exception("明文数据已损坏");  
        }  
    }  
  
    /** 
     * 解密过程 
     * @param privateKey 私钥 
     * @param cipherData 密文数据 
     * @return 明文 
     * @throws Exception 解密过程中的异常信息 
     */  
    public byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData) throws Exception{  
        if (privateKey== null){  
            throw new Exception("解密私钥为空, 请设置");  
        }  
        Cipher cipher= null;  
        try {  
            cipher= Cipher.getInstance("RSA");//, new BouncyCastleProvider());  
            cipher.init(Cipher.DECRYPT_MODE, privateKey);  
            byte[] output= cipher.doFinal(cipherData);  
            return output;  
        } catch (NoSuchAlgorithmException e) {  
            throw new Exception("无此解密算法");  
        } catch (NoSuchPaddingException e) {  
            e.printStackTrace();  
            return null;  
        }catch (InvalidKeyException e) {  
            throw new Exception("解密私钥非法,请检查");  
        } catch (IllegalBlockSizeException e) {  
            throw new Exception("密文长度非法");  
        } catch (BadPaddingException e) {  
            throw new Exception("密文数据已损坏");  
        }         
    }  
  
    
    
    /** 
     * 字节数据转字符串专用集合 
     */  
    private static final char[] HEX_CHAR= {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'}; 
    
    /** 
     * 字节数据转十六进制字符串 
     * @param data 输入数据 
     * @return 十六进制内容 
     */  
    public static String byteArrayToString(byte[] data){  
        StringBuilder stringBuilder= new StringBuilder();  
        for (int i=0; i<data.length; i++){  
            //取出字节的高四位 作为索引得到相应的十六进制标识符 注意无符号右移  
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0)>>> 4]);  
            //取出字节的低四位 作为索引得到相应的十六进制标识符  
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);  
            if (i<data.length-1){  
                stringBuilder.append(' ');  
            }  
        }  
        return stringBuilder.toString();  
    }  
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 // ___________________________________________ Begin Test  ___________________________________________
    
    // cat rsa_public_key.pem 
    public static final String DEFAULT_PUBLIC_KEY=   
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCeb4zRZnruPoX0tzbF41HEuZ56" + "\r" +
            "Oyx7DkHKYfgpu4Oo4CrhBZy+im5vGad997A4cE0bqjytgO6KCy5N/PDit75msNfg" + "\r" +
            "iUB2UXtzAuc6vSh+5aSF7xqMOserCqxIfx9uU6eItuRbFD7FlyR7zNUcSmgbCf4x" + "\r" +
            "FE7qoIK1PmYWHGF6RwIDAQAB" + "\r";

    // cat pkcs8_private_key.pem
    public static final String DEFAULT_PRIVATE_KEY=
            "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJ5vjNFmeu4+hfS3" + "\r" +
            "NsXjUcS5nno7LHsOQcph+Cm7g6jgKuEFnL6Kbm8Zp333sDhwTRuqPK2A7ooLLk38" + "\r" +
            "8OK3vmaw1+CJQHZRe3MC5zq9KH7lpIXvGow6x6sKrEh/H25Tp4i25FsUPsWXJHvM" + "\r" +
            "1RxKaBsJ/jEUTuqggrU+ZhYcYXpHAgMBAAECgYAiSjJH/84LUizb0etg2NoRobrS" + "\r" +
            "6Yuu9l721k1HevX7zsh4+yX5dyx/jyXY9YqGLIgDvMx1ILp/1funlV7tMqdaay5n" + "\r" +
            "PF8S0xzndL7IJrjYLFZKz2V41/BaxhBdXK4W7IpbOnLlGPHB9o0iMRNiEwh4OkDd" + "\r" +
            "QgHGXKRpRVVLMmeYAQJBAMqkZy/SJtKJrvwzygTvkaEwYmw1GdkRc1fFaX7XFHVV" + "\r" +
            "lFrGt2jT6amXWsMvf8eMyQONH76fm2A+xZUna4ZwIMcCQQDIJ01CyuC19g0Oh5o0" + "\r" +
            "O9WjfRsts8Xie/rwpK32ZuVxt5uebHu/y/rSRztZP53W2Dq6wzu7o+Vxc580muu1" + "\r" +
            "zVqBAkBk5JITtzhpHvAm1cpBxt3lOWfnpFCoSQ36p2mtI30mJmPlBoePA+OU8qzX" + "\r" +
            "/bBLNIdo4zzo9iKwOtC5QJVCrFVFAkBE9R1uPJ1ss2fOMLPU+SRinjCl70DnBdXv" + "\r" +
            "4Jy6vrqgEiUAUNnVu34fwkDVP9Cue3LIc4j53b6n9rDMG+/HhAeBAkAvbv5C2or/" + "\r" +
            "tz5bTpYoe7VgXXAOJJ/T8Er7w+kLzusA1h4dXEewvVTcpZEnwNHagERMEGoihCei" + "\r" +
            "4Q6TqiEiTTfW" + "\r";
    
    public static void main(String[] args){  
        
//            String privateKeyPath = "/Users/Isaacs/Desktop/RSA_KEYS/rsa_public_key.pem";        // replace your public key path here
//            String publicKeyPath =  "/Users/Isaacs/Desktop/RSA_KEYS/pkcs8_private_key.pem";     // replace your private path here
//            RSAEncryptor rsaEncryptor = new RSAEncryptor(privateKeyPath, publicKeyPath);
        
        RSAEncryptor rsaEncryptor= new RSAEncryptor();  
//        rsaEncryptor.genKeyPair();    // use this to generate key pairs, or,  use the following codes to load the key pairs with the specified keys you that created by openssl
        
        //Load The Public Key  
        try {  
            rsaEncryptor.loadPublicKey(RSAEncryptor.DEFAULT_PUBLIC_KEY);  
            System.out.println("Load Public Key Successfully, length："+rsaEncryptor.getPublicKey().toString().length());
        } catch (Exception e) {  
            System.err.println(e.getMessage());  
            System.err.println("Load Public Key Failed");  
        }  
        //Load The Private Key  
        try {  
            rsaEncryptor.loadPrivateKey(RSAEncryptor.DEFAULT_PRIVATE_KEY);  
            System.out.println("Load Private Key Successfully, length："+rsaEncryptor.getPrivateKey().toString().length());
        } catch (Exception e) {  
            System.err.println(e.getMessage());  
            System.err.println("Load Private Key Failed");  
        }  
        
        
        
        // After Loaded Public and Private Keys .....
        
        
        
        // ___________________________________________ The First Test
  
        System.out.println("\n___________________________________________ The First Test\n");
        try {
            
            String test = "JAVA";
            String testRSAEnWith64 = rsaEncryptor.encryptWithBase64(test);
            String testRSADeWith64 = rsaEncryptor.decryptWithBase64(testRSAEnWith64);
            System.out.println("\nEncrypt: \n" + testRSAEnWith64);
            System.out.println("\nDecrypt: \n" + testRSADeWith64);
            
            // NSLog the encrypt string from Xcode , and paste it here.
            String rsaBase46StringFromIOS =
                    "nIIV7fVsHe8QquUbciMYbbumoMtbBuLsCr2yMB/WAhm+S/kGRPlf+k2GH8imZIYQ" + "\r" +
                    "QBDssVLQmS392QlxS87hnwMRJIzWw6vdRv/k79TgTfu6tI/9QTqIOvNlQIqtIcVm" + "\r" +
                    "R/suvydoymKgdlB+ce5/tHSxfqEOLLrL1Zl2PqJSP4A=";
            
            String decryptStringFromIOS = rsaEncryptor.decryptWithBase64(rsaBase46StringFromIOS);
            System.out.println("Decrypt result from ios client: \n" + decryptStringFromIOS);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        
        
        // ___________________________________________ The second Test
        
        System.out.println("\n___________________________________________ The second Test\n");
        try {  
            
            String encryptStr= "Test123.";  
            //encrypt 
            byte[] cipher = rsaEncryptor.encrypt(rsaEncryptor.getPublicKey(), encryptStr.getBytes());  
            //decrypt 
            byte[] plainText = rsaEncryptor.decrypt(rsaEncryptor.getPrivateKey(), cipher);  
            
            System.out.println("\nBefore Encrypt: \n" + encryptStr);
            System.out.println("\nAfter Encrypt: \n" + new BASE64Encoder().encode(cipher));  // here no Base64 encode 
            System.out.println("\nDecrypt: \n" + new String(plainText)); 
            
        } catch (Exception e) {  
            System.err.println(e.getMessage());  
        }  
        
    }
   
 // ___________________________________________ End Test  ___________________________________________
   
}
