package com.zzs.TJBmatch.services;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class ImgSer {

	//// create thumnail JPG image of 100x100 pixels
	public static void createThumnailFIle(String imgFile,String outFile) throws IOException{	
		BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		img.createGraphics().drawImage(ImageIO.read(new File(imgFile)).getScaledInstance(100, 100, Image.SCALE_SMOOTH),0,0,null);
		ImageIO.write(img, "jpg", new File(outFile));
	}
	
	public static void createThumnailFIle(String imgFile,String outFile,int iwpx,int ihpx) throws IOException{	
		BufferedImage img = new BufferedImage(iwpx, ihpx, BufferedImage.TYPE_INT_RGB);
		img.createGraphics().drawImage(ImageIO.read(new File(imgFile)).getScaledInstance(100, 100, Image.SCALE_SMOOTH),0,0,null);
		ImageIO.write(img, "jpg", new File(outFile));
	}

	////20200405 lj add
	public static void createThumnailFIle(String imgFile,String outFile,int iwpx1,int ihpx1,int iwpx2,int ihpx2) throws IOException{
		BufferedImage img = new BufferedImage(iwpx1, ihpx1, BufferedImage.TYPE_INT_RGB);
		img.createGraphics().drawImage(ImageIO.read(new File(imgFile)).getScaledInstance(iwpx2, ihpx2, Image.SCALE_SMOOTH),0,0,null);
		ImageIO.write(img, "jpg", new File(outFile));
	}


	////simplest way to make thumnail of 100x100 pixels JAVA -zzs
	public static Image createThumnailImg(String imgFile) throws IOException {
		return ImageIO.read(new File(imgFile)).getScaledInstance(100, 100, Image.SCALE_SMOOTH);	
	}
	
	public static Image createThumnailImg(String imgFile,int iwpx,int ihpx) throws IOException {
		return ImageIO.read(new File(imgFile)).getScaledInstance(iwpx, ihpx, Image.SCALE_SMOOTH);	
	}

	public static Image createThumnailImg(Image img,int iwpx,int ihpx) throws IOException {
		return img.getScaledInstance(iwpx, ihpx, Image.SCALE_SMOOTH);
	}

	
	public static BufferedImage scaleImage(BufferedImage source,double ratio) {
		  int w = (int) (source.getWidth() * ratio);
		  int h = (int) (source.getHeight() * ratio);
		  BufferedImage bi = getCompatibleImage(w, h);
		  Graphics2D g2d = bi.createGraphics();
		  double xScale = (double) w / source.getWidth();
		  double yScale = (double) h / source.getHeight();
		  AffineTransform at = AffineTransform.getScaleInstance(xScale,yScale);
		  g2d.drawRenderedImage(source, at);
		  g2d.dispose();
		  return bi;
	}
	
	public static void scaleImageFile(String sourceFile,double ratio,String destFile) throws IOException {
		  BufferedImage source = ImageIO.read(new File(sourceFile));
		  int w = (int) (source.getWidth() * ratio);
		  int h = (int) (source.getHeight() * ratio);
		  BufferedImage bi = getCompatibleImage(w, h);
		  Graphics2D g2d = bi.createGraphics();
		  double xScale = (double) w / source.getWidth();
		  double yScale = (double) h / source.getHeight();
		  AffineTransform at = AffineTransform.getScaleInstance(xScale,yScale);
		  g2d.drawRenderedImage(source, at);
		  g2d.dispose();
		  try {
			ImageIO.write(bi, "jpg", new File(destFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static BufferedImage getCompatibleImage(int w, int h) {
		  GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		  GraphicsDevice gd = ge.getDefaultScreenDevice();
		  GraphicsConfiguration gc = gd.getDefaultConfiguration();
		  BufferedImage image = gc.createCompatibleImage(w, h);
		  return image;
	}
	
	@SuppressWarnings("unused")
	private static void validateDimensions(int width, int height)
	{
		if (width <= 0 && height <= 0)
		{
			throw new IllegalArgumentException(
					"Destination image dimensions must not be less than " +
					"0 pixels."
			);
		}
		else if (width <= 0 || height <= 0)
		{
			String dimension = width == 0 ? "width" : "height";
			
			throw new IllegalArgumentException(
					"Destination image " + dimension + " must not be " +
					"less than or equal to 0 pixels."
			);
		}		
	}


	///////20200318 lj 图片转成Base64
	public static String getImgStr(String imgFile) {
		// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		InputStream in = null;
		byte[] data = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			return "";
		}
		Base64.Encoder encoder = Base64.getEncoder();
		String result = encoder.encodeToString(data);
		return result;
	}

	////// 2020/04/08 zj 图片base64保存
	public static boolean SaveImgByBase64(String base64, String filePath) {
		if (base64.isEmpty() ||  filePath.isEmpty()) {
			return false;
		}
		try {
			String n_base64 = base64.replaceAll(" ","+");
//			System.out.println(n_base64);
//			System.out.println(n_base64.length());
//			byte[] decodedImg = Base64.getDecoder().decode(n_base64.getBytes(StandardCharsets.UTF_8));
//			Files.write(Paths.get(filePath), decodedImg);

			Files.write(Paths.get(filePath), Base64.getDecoder().decode(n_base64.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/////20210123 bql 删除文件下的图片
	public static void deleteFile(File file) {
		File[] listFiles = file.listFiles();
		for (File subFile : listFiles)
		{
			if (subFile.isDirectory())
				deleteFile(subFile);
			else
				subFile.delete();
		}
	}

}
