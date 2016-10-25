package com.hitangjun.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class EditImage {
	public static Logger log = LoggerFactory.getLogger(EditImage.class);
	
	public static BufferedImage resize(BufferedImage source, double scale) {
		int targetW = (int) (source.getWidth() * scale);
		int targetH = (int) (source.getHeight() * scale);
		return compressPic(source,targetW,targetH);
//		return resize(source,targetW,targetH);
	}
			
//	public static BufferedImage resize(BufferedImage source, int targetW,
//			int targetH) {
//		int type = source.getType();
//		BufferedImage target = null;
//		double sx = (double) targetW / source.getWidth();
//		double sy = (double) targetH / source.getHeight();
//
//		if (sx > sy) {
//			sx = sy;
//			targetW = (int) (sx * source.getWidth());
//		} else {
//			sy = sx;
//			targetH = (int) (sy * source.getHeight());
//		}
//		if (type == BufferedImage.TYPE_CUSTOM) { // handmade
//			ColorModel cm = source.getColorModel();
//			WritableRaster raster = cm.createCompatibleWritableRaster(targetW,
//					targetH);
//			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
//			target = new BufferedImage(cm, raster, alphaPremultiplied, null);
//		} else
//			target = new BufferedImage(targetW, targetH, type);
//		Graphics2D g = target.createGraphics();
//		// smoother than exlax:
//		g.setRenderingHint(RenderingHints.KEY_RENDERING,
//				RenderingHints.VALUE_RENDER_QUALITY);
//		g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
//		g.dispose();
//		return target;
//	}
	
	// 图片处理 
	public static BufferedImage compressPic(BufferedImage source, int targetW,
			int targetH) { 
		return compressPic(source, targetW, targetH,true);
	}
	 public static BufferedImage compressPic(BufferedImage source, int targetW,
				int targetH,boolean proportion) { 
		 try { 
			 //获得源文件 
			 Image img = source; 
			 // 判断图片格式是否正确 
			 if (img.getWidth(null) == -1) {
				 System.out.println(" can't read,retry!" + "<BR>"); 
				 return null; 
			 } else { 
				 int newWidth; int newHeight; 
				 // 判断是否是等比缩放 
				 if (proportion) { 
					 // 为等比缩放计算输出的图片宽度及高度 
					 double rate1 = ((double) img.getWidth(null)) / (double) targetW + 0.1; 
					 double rate2 = ((double) img.getHeight(null)) / (double) targetH + 0.1; 
					 // 根据缩放比率大的进行缩放控制 
					 double rate = rate1 > rate2 ? rate1 : rate2; 
					 newWidth = (int) (((double) img.getWidth(null)) / rate); 
					 newHeight = (int) (((double) img.getHeight(null)) / rate); 
				 } else { 
					 newWidth = targetW; // 输出的图片宽度 
					 newHeight = targetH; // 输出的图片高度 
				 } 
			 	BufferedImage tag = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_INT_RGB); 
			 	
			 	/*
				 * Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的
				 * 优先级比速度高 生成的图片质量比较好 但速度慢
				 */ 
			 	tag.getGraphics().drawImage(img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
			 	
			 	
//			 	FileOutputStream out = new FileOutputStream("/"+System.currentTimeMillis()+".png");
//			 	// JPEGImageEncoder可适用于其他图片类型的转换 
//			 	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
//			 	encoder.encode(tag); 
//			 	out.close(); 
			 	
			 	return tag;
			 } 
		 } catch (Exception ex) { 
			 ex.printStackTrace(); 
		 } 
		 return null; 
	}

	public static void saveImageAsJpg(String fromFileStr, String saveToFileStr,
			int width, int hight) throws Exception {
		BufferedImage srcImage;
		// String ex =
		// fromFileStr.substring(fromFileStr.indexOf("."),fromFileStr.length());
		String imgType = "JPEG";
		if (fromFileStr.toLowerCase().endsWith(".png")) {
			imgType = "PNG";
		}
		File saveFile = new File(saveToFileStr);
		File fromFile = new File(fromFileStr);
		srcImage = ImageIO.read(fromFile);
		if (width > 0 || hight > 0) {
			srcImage = compressPic(srcImage, width, hight);
		}
//		ImageIO.write(srcImage, imgType, saveFile);
		
		FileOutputStream out = new FileOutputStream(saveFile);
	 	// JPEGImageEncoder可适用于其他图片类型的转换 
	 	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
	 	encoder.encode(srcImage); 
	 	out.close(); 
		

	}

	public static void saveImageAsJpg(String fromFileStr, String saveToFileStr,
			double scale) throws Exception {
		BufferedImage srcImage;
		// String ex =
		// fromFileStr.substring(fromFileStr.indexOf("."),fromFileStr.length());
		String imgType = "JPEG";
		if (fromFileStr.toLowerCase().endsWith(".png")) {
			imgType = "PNG";
		}
		File saveFile = new File(saveToFileStr);
		File fromFile = new File(fromFileStr);
		srcImage = ImageIO.read(fromFile);
		if (scale > 0) {
			srcImage = resize(srcImage, scale);
		}
//		ImageIO.write(srcImage, imgType, saveFile);
		FileOutputStream out = new FileOutputStream(saveFile);
	 	// JPEGImageEncoder可适用于其他图片类型的转换 
	 	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
	 	encoder.encode(srcImage); 
	 	out.close(); 
	}
	
	/**
	 * 将一个图片按指定值压缩，返回数据流数组
	 * @param fromFileStr 原始文件路径
	 * @param imgType 图片类型
	 * @param scale 压缩比率
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] compressImg(File fromFile, String imgType,
			double scale) throws Exception {
		ByteArrayOutputStream  out = new ByteArrayOutputStream();
		BufferedImage srcImage = ImageIO.read(fromFile);
		imgType = imgType.toUpperCase();
		if (scale > 0) {
			srcImage = resize(srcImage, scale);
		}
//		ImageIO.write(srcImage, imgType, out);
		
	 	// JPEGImageEncoder可适用于其他图片类型的转换 
	 	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
	 	encoder.encode(srcImage); 
	 	out.close();
		
		return out.toByteArray();
	}
	
	/**
	 * 将一个图片按指定宽度高度压缩，返回数据流数组
	 * @param fromFileStr 原始文件路径
	 * @param imgType 图片类型
	 * @param int width,int height 宽度高度
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] compressImg(File fromFile, String imgType,int width,int height )
			throws Exception {
		ByteArrayOutputStream  out = new ByteArrayOutputStream();
		BufferedImage srcImage = ImageIO.read(fromFile);
		
		imgType = imgType.toUpperCase();
		
		srcImage = compressPic(srcImage,width,height );
		
		// JPEGImageEncoder可适用于其他图片类型的转换 
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
		encoder.encode(srcImage); 
		out.close(); 
		return out.toByteArray();
	}
	
	/**
	 * 将一个图片按指定宽度高度压缩，返回数据流数组
	 * @param fromFileStr 原始文件路径
	 * @param imgType 图片类型
	 * @param int width,int height 宽度高度
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] compressImgFromStream(InputStream input, String imgType,int width,int height )
			throws Exception {
		ByteArrayOutputStream  out = new ByteArrayOutputStream();
		BufferedImage srcImage = ImageIO.read(input);
		imgType = imgType.toUpperCase();
		
		srcImage = compressPic(srcImage,width,height );
		
		// JPEGImageEncoder可适用于其他图片类型的转换 
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
		encoder.encode(srcImage); 
		out.close(); 
		return out.toByteArray();
	}
	

	public static Color getRandColor(int fc,int bc){
	    Random random = new Random();
	    if(fc>255) fc=255;
	    if(bc>255) bc=255;
	    int r=fc+random.nextInt(bc-fc);
	    int g=fc+random.nextInt(bc-fc);
	    int b=fc+random.nextInt(bc-fc);
	    return new Color(r,g,b);
	    }

	
	
	/**定点裁剪图片*/
	public static byte[] cropImage(InputStream is,int x,int y,int width,int height,String imgType){
        ImageInputStream iis =null ;
		try {
			imgType = imgType.toUpperCase();
			
			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(imgType); 
	        ImageReader reader = it.next();
	      //获取图片流
            iis = ImageIO.createImageInputStream(is);
			
            
            /**
             * <p>iis:读取源.true:只向前搜索 </p>.将它标记为 ‘只向前搜索’。
             * 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许 reader
             *  避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
             */
            reader.setInput(iis,true) ;
           
            /**
             * <p>描述如何对流进行解码的类<p>.用于指定如何在输入时从 Java Image I/O
             * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件
             * 将从其 ImageReader 实现的 getDefaultReadParam 方法中返回
             * ImageReadParam 的实例。 
             */
            ImageReadParam param = reader.getDefaultReadParam();
            
            /**
             * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
             * 的左上顶点的坐标（x，y）、宽度和高度可以定义这个区域。
             */
            Rectangle rect = new Rectangle(x, y, width, height);
             
            //提供一个 BufferedImage，将其用作解码像素数据的目标。
            param.setSourceRegion(rect);

            /**
             * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将
             * 它作为一个完整的 BufferedImage 返回。
             */
            BufferedImage bi = reader.read(0,param);               
     
			// JPEGImageEncoder可适用于其他图片类型的转换 
            ByteArrayOutputStream  out = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
			encoder.encode(bi); 
			out.close(); 
			
			return out.toByteArray();
		
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(is!=null)
				   is.close() ;      
			    if(iis!=null)
			       iis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}  
		}
		return null;
		
	}
	
	/**从中心点截取图片*/
	public static byte[] centerCropImg(InputStream input,int width,int height,String imgType){
		
		ImageInputStream iis =null ;
		try {
			imgType = imgType.toUpperCase();
			
			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(imgType); 
	        ImageReader reader = it.next();
	        
	        byte[] data = compressImgFromStream(input, imgType, width+100, height+100);
	        
            iis = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
			
            
            /**
             * <p>iis:读取源.true:只向前搜索 </p>.将它标记为 ‘只向前搜索’。
             * 此设置意味着包含在输入源中的图像将只按顺序读取，可能允许 reader
             *  避免缓存包含与以前已经读取的图像关联的数据的那些输入部分。
             */
            reader.setInput(iis,true) ;
           
            /**
             * <p>描述如何对流进行解码的类<p>.用于指定如何在输入时从 Java Image I/O
             * 框架的上下文中的流转换一幅图像或一组图像。用于特定图像格式的插件
             * 将从其 ImageReader 实现的 getDefaultReadParam 方法中返回
             * ImageReadParam 的实例。 
             */
            ImageReadParam param = reader.getDefaultReadParam();
            
            BufferedImage srcImage = reader.read(0,param); 
            int imgWidth = srcImage.getWidth();
			int imgHeight = srcImage.getHeight();
			
			//计算图片的中心点
			int x,y;
			if(imgWidth < width){
				x = 0;
			}else{
				x = (imgWidth - width)/2;
			}
			if(imgHeight < height){
				y = 0;
			}else{
				y = (imgHeight - height)/2;
			}
            
            /**
             * 图片裁剪区域。Rectangle 指定了坐标空间中的一个区域，通过 Rectangle 对象
             * 的左上顶点的坐标（x，y）、宽度和高度可以定义这个区域。
             */
            Rectangle rect = new Rectangle(x, y, width, height);
             
            //提供一个 BufferedImage，将其用作解码像素数据的目标。
            param.setSourceRegion(rect);

            /**
             * 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象，并将
             * 它作为一个完整的 BufferedImage 返回。
             */
            BufferedImage bi = reader.read(0,param);               
     
            ByteArrayOutputStream  out = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out); 
			encoder.encode(bi); 
			out.close(); 
			return out.toByteArray();
		
		} catch (Exception e) {
			log.error("先压缩再中心截取 error:",e);
		}finally{
			try {
				if(input!=null)
					input.close() ;      
			    if(iis!=null)
			       iis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}  
		}
		return null;
			
	}
	
	public static void main(String argv[]) {
		try {
			 EditImage.saveImageAsJpg("e:/1.jpg", "e:/323.jpeg", 300, 300);
			 

//			File f= new File(src);
//			String dest = src.substring(0,src.lastIndexOf("."))+"_mini_1.jpg";
//			System.out.println(dest);
//			
//			EditImage
//					.saveImageAsJpg(
//							src,
//							dest,
//							2.5);
//			
//			
//			
//			
//			byte[] data = EditImage.centerCropImg(new FileInputStream("d:/2.jpg"),200,200,"jpeg");
//			FileOutputStream out = new FileOutputStream("d:/2_crop.jpg");
//			out.write(data);
//			out.close();
//			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
