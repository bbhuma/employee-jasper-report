//package com.example.demo;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.StringWriter;
//import java.lang.reflect.Field;
//import java.security.KeyStore;
//import java.security.PrivateKey;
//import java.security.Security;
//import java.security.cert.Certificate;
//import java.security.cert.X509Certificate;
//import java.time.LocalDate;
//import java.util.Base64;
//import java.util.Date;
//import java.util.HashSet;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.velocity.VelocityContext;
//import org.apache.velocity.app.VelocityEngine;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import com.itextpdf.html2pdf.HtmlConverter;
//import com.itextpdf.kernel.pdf.EncryptionConstants;
//import com.itextpdf.kernel.pdf.PdfDocument;
//import com.itextpdf.kernel.pdf.PdfPage;
//import com.itextpdf.kernel.pdf.PdfReader;
//import com.itextpdf.kernel.pdf.PdfWriter;
//import com.itextpdf.kernel.pdf.ReaderProperties;
//import com.itextpdf.kernel.pdf.StampingProperties;
//import com.itextpdf.kernel.pdf.WriterProperties;
//import com.itextpdf.layout.Document;
//import com.itextpdf.signatures.BouncyCastleDigest;
//import com.itextpdf.signatures.IExternalDigest;
//import com.itextpdf.signatures.IExternalSignature;
//import com.itextpdf.signatures.PdfSigner;
//import com.itextpdf.signatures.PrivateKeySignature;
//import com.modefin.kcb.ess.dao.AccountDao;
//import com.modefin.kcb.ess.dao.CustomersService;
//import com.modefin.kcb.ess.dao.RetailTransactionService;
//import com.modefin.kcb.ess.dao.ScheduledStatementRequestDao;
//import com.modefin.kcb.ess.dao.TemplateService;
//import com.modefin.kcb.ess.interfaces.GenerateStatement;
//import com.modefin.kcb.ess.model.Account;
//import com.modefin.kcb.ess.model.Customers;
//import com.modefin.kcb.ess.model.ScheduledStatementRequest;
//import com.modefin.kcb.ess.util.ESSConstants;
//import com.modefin.kcb.ess.util.ESSUtil;
//
//import jakarta.transaction.Transaction;
//
//@Service
//@Component
//public class GeneratePDFStatement implements GenerateStatement {
//
//	private static final Logger log = LoggerFactory.getLogger(GeneratePDFStatement.class);
//
//	@Autowired
//	TemplateService templateService;
//
//	@Autowired
//	RetailTransactionService retailService;
//
//	@Autowired
//	ScheduledStatementRequestDao onDemandService;
//
//	@Autowired
//	CustomersService customerService;
//
//	@Autowired
//	AccountDao accountService;
//
//	@Value("${report.file.path}")
//	String filePath;
//
//	@Value("${report.file.maxpages}")
//	int maxPages;
//
//	@Value("${report.file.keystorepath}")
//	String keystorePath;
//
//	@Value("${report.file.keystorepassword}")
//	String keystorePassword;
//
//	@Value("${report.file.keypassword}")
//	String keyPassword;
//
//	@Value("${report.file.keyalias}")
//	String keyAlias;
//
//	@Value("${report.file.importantNote}")
//	String importantNote;
//
//	@Async
//	@Override
//	public boolean process(ScheduledStatementRequest onDemandSatementRequest) {
//		CompletableFuture.supplyAsync(() -> {
//			try {
//				log.info("Start Time :: " + new Date().toString());
//				String templateName = ESSUtil.getTemplateName(onDemandSatementRequest);
//				String template = templateService.getDataFromCache(templateName);
//
//				log.info("template :: " + template);
//
//				Customers customer = customerService.getCustomerByCustId(onDemandSatementRequest.getCustomerId());
//				Account account = accountService.getAccountByAccountNumber(onDemandSatementRequest.getAccountNumber());
//
//				VelocityEngine velocityEngine = new VelocityEngine();
//				velocityEngine.init();
//				log.info("Fetching transactions");
//
//				VelocityContext context = new VelocityContext();
//
//				List<Transaction> transactions = retailService.getTransactionsByCustomerAndDateRange(
//						onDemandSatementRequest.getCustomerId(), onDemandSatementRequest.getStartDate(),
//						onDemandSatementRequest.getEndDate());
//
//				context.put("transactions", transactions);
//
//				log.info("Received transactions");
//
//				context.put("currentDate", new Date().toString());
//				context.put("currency", "KSH");
//				context.put("accountNumber", onDemandSatementRequest.getAccountNumber());
//				context.put("accountType", onDemandSatementRequest.getCustomerCategory());
//				context.put("customerName", customer.getCustomerName());
//				context.put("customerAddress", customer.getPostalAddress());
//
//				context.put("statementPeriod",
//						onDemandSatementRequest.getStartDate() + "-" + onDemandSatementRequest.getEndDate());
//				context.put("kcbLogo", "kcb-logo.svg");
//				context.put("baseBranch", account.getBranch());
//				context.put("startDate", onDemandSatementRequest.getStartDate());
//				context.put("endDate", onDemandSatementRequest.getEndDate());
//				context.put("reportId", onDemandSatementRequest.getRequestId());
//
//				context.put("importantNote", importantNote);
//
//				context.put("accountType", account.getAccountType());
//				context.put("startBalance", account.getOpeningBalance());
//				context.put("statementTitle", onDemandSatementRequest.getCustomerCategory() + " "
//						+ onDemandSatementRequest.getCustomerModule() + " Statement");
//
//				String fileName = ESSUtil.generateReportName(onDemandSatementRequest.getCustomerCategory());
//
//				StringWriter writer = new StringWriter();
//				velocityEngine.evaluate(context, writer, "TransactionReport",
//						new String(Base64.getDecoder().decode(template)));
//
//				String renderedHtml = writer.toString();
//
//				int pageCount = 0;
//				int pdfCounter = 1;
//
//				Document document = null;
//				PdfWriter pdfWriter = null;
//				PdfDocument pdfDoc = null;
//
//				HtmlConverter.convertToPdf(renderedHtml,
//						new FileOutputStream(filePath + "temp_output" + fileName + ".pdf"));
//				PdfDocument tempPdfDoc = new PdfDocument(new PdfReader(filePath + "temp_output" + fileName + ".pdf"));
//				String password = "password";
//				HashSet<String> namesList = new HashSet<String>();
//				for (int i = 1; i <= tempPdfDoc.getNumberOfPages(); i++) {
//					if (pageCount == 0 || pageCount >= maxPages) {
//						if (pdfDoc != null) {
//							document.close();
//							pdfDoc.close();
//						}
//						String outputPdf = filePath + fileName + pdfCounter + ".pdf";
//						log.info("outputPdf->" + outputPdf);
//						namesList.add(outputPdf);
//						pdfCounter++;
//						pdfWriter = new PdfWriter(new FileOutputStream(outputPdf),
//								new WriterProperties().setStandardEncryption(password.getBytes(), password.getBytes(),
//										EncryptionConstants.ALLOW_PRINTING, EncryptionConstants.ENCRYPTION_AES_256));
//						pdfDoc = new PdfDocument(pdfWriter);
//						document = new Document(pdfDoc);
//						pageCount = 0;
//					}
//					PdfPage page = tempPdfDoc.getPage(i);
//					pdfDoc.addPage(page.copyTo(pdfDoc));
//					pageCount++;
//				}
//				if (document != null) {
//					document.close();
//				}
//				if (pdfDoc != null) {
//					pdfDoc.close();
//				}
//				if (tempPdfDoc != null) {
//					tempPdfDoc.close();
//				}
//				if (pdfWriter != null) {
//					pdfWriter.close();
//				}
//				File tempFile = new File(filePath + "temp_output" + fileName + ".pdf");
//				if (tempFile.exists()) {
//					tempFile.delete();
//				}
//
//				StringBuilder sb = new StringBuilder();
//				for (String element : namesList) {
//					sb.append(element).append(", ");
//				}
//				signPdf(namesList);
//				log.info("PDF generated successfully!" + fileName);
//				log.info("End Time :: " + new Date().toString());
//				onDemandSatementRequest.setGeneratedFilePath(sb.toString());
//				onDemandSatementRequest.setGeneratedAt(LocalDate.now());
//				onDemandSatementRequest.setStatus(ESSConstants.generated);
//				onDemandService.updateRecord(onDemandSatementRequest);
//				return true;
//			} catch (Exception e) {
//				onDemandSatementRequest.setStatus(ESSConstants.failed);
//				onDemandSatementRequest.setFailureReason(e.getMessage());
//				onDemandService.updateRecord(onDemandSatementRequest);
//				e.printStackTrace();
//				return false;
//			}
//		}).completeOnTimeout(false, 600, TimeUnit.SECONDS);
//		return false;
//	}
//
//
//	public static String[] getFieldNames(Class<?> clazz) {
//		Field[] fields = clazz.getDeclaredFields();
//		String[] fieldNames = new String[fields.length];
//		for (int i = 0; i < fields.length; i++) {
//			fieldNames[i] = fields[i].getName();
//		}
//		return fieldNames;
//	}
//
//	private void signPdf(HashSet<String> files) throws Exception {
//
//		Security.addProvider(new BouncyCastleProvider());
//		KeyStore ks = KeyStore.getInstance("PKCS12");
//		ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
//
//		PrivateKey privateKey = (PrivateKey) ks.getKey(keyAlias, keyPassword.toCharArray());
//		Certificate[] chain = ks.getCertificateChain(keyAlias);
//		X509Certificate cert = (X509Certificate) chain[0];
//		StampingProperties stampingProperties = new StampingProperties();
//		stampingProperties.useAppendMode();
//		stampingProperties.preserveEncryption();
//		for (String element : files) {
//			PdfReader reader = new PdfReader(element, new ReaderProperties().setPassword("password".getBytes()));
//			String dest = element.replace(".pdf", "s.pdf");
//			PdfSigner signer = new PdfSigner(reader, new FileOutputStream(new File(dest)), stampingProperties);
//			IExternalSignature externalSignature = new PrivateKeySignature(privateKey, "SHA-256",
//					BouncyCastleProvider.PROVIDER_NAME);
//			IExternalDigest externalDigest = new BouncyCastleDigest();
//
//			signer.signDetached(externalDigest, externalSignature, chain, null, null, null, 0,
//					PdfSigner.CryptoStandard.CMS);
//
//			File originalFile = new File(element);
//			if (originalFile.delete()) {
//				log.info("Signature Successful");
//			} else {
//				System.out.println("Signature failed");
//			}
//		}
//	}
//}
