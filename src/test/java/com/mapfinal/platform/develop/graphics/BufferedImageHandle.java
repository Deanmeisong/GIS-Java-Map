package com.mapfinal.platform.develop.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import com.mapfinal.event.Callback;
import com.mapfinal.event.Event;
import com.mapfinal.kit.FileKit;
import com.mapfinal.resource.image.ImageHandle;
import com.mapfinal.task.ThreadPool;

public class BufferedImageHandle extends ImageHandle<BufferedImage> {
	
	@Override
	public int getWidth(BufferedImage image) {
		// TODO Auto-generated method stub
		return image.getWidth();
	}

	@Override
	public int getHeight(BufferedImage image) {
		// TODO Auto-generated method stub
		return image.getHeight();
	}

	@Override
	public BufferedImage readFile(String fileName) {
		// TODO Auto-generated method stub
		try {
			String lockFileName = fileName + ".lock";
			File lockFile = new File(lockFileName);
			File file = new File(fileName);
			if(file.exists() && !lockFile.exists()) {
				return ImageIO.read(file);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public byte[] read(String fileName) {
		// TODO Auto-generated method stub
		try {
			String lockFileName = fileName + ".lock";
			File lockFile = new File(lockFileName);
			File file = new File(fileName);
			if(file.exists() && !lockFile.exists()) {
				return Files.readAllBytes(file.toPath());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void writeFile(String fileName, BufferedImage image) {
		// TODO Auto-generated method stub
		try {
			//add file write lock
			String lockFileName = fileName + ".lock";
			File lockFile = new File(lockFileName);
			lockFile.createNewFile();
			ImageIO.write(image, FileKit.getExtensionName(fileName), new File(fileName));
			lockFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void write(String fileName, byte[] image) {
		// TODO Auto-generated method stub
		FileOutputStream fs = null;
		try {
			//add file write lock
			String lockFileName = fileName + ".lock";
			File lockFile = new File(lockFileName);
			lockFile.createNewFile();
			fs=new FileOutputStream(fileName, false);
			fs.write(image);
			fs.close();
			lockFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void download(String url, Callback callback) {
		// TODO Auto-generated method stub
		//?????????
//		ThreadPool.getInstance().submit(new Runnable() {
//			@Override
//			public void run() {
				// TODO Auto-generated method stub
				HttpURLConnection httpCon = null;
		        URLConnection  con = null;
		        URL urlObj=null;
		        InputStream in =null;
		        try {
		        	//System.out.println("[GraphicsImageIO] httpImage: " + url);
		        	urlObj = new URL(url);
					con = urlObj.openConnection();
					httpCon =(HttpURLConnection) con;
					// Allow Inputs
					httpCon.setDoInput(true);
			        // Don't use a cached copy.
					httpCon.setUseCaches(false);
					httpCon.setConnectTimeout(5000);//10000
					httpCon.setReadTimeout(5000);//240000-180 sec
					httpCon.setRequestProperty("Accept", "*/*");
		            httpCon.setRequestMethod("GET");
					int responseCode = httpCon.getResponseCode();
		            if (responseCode == 200) {
		            	in = httpCon.getInputStream();
				        BufferedImage image = ImageIO.read(in);
				        if(callback!=null) callback.execute(new Event("imageCache:get").set("image", image));
				        in.close();
		            }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					if(callback!=null) callback.error(new Event("imageCache:get").set("image", null).set("error", e));
				}
//			}
//		});
//		ThreadPool.getInstance().release();
	}

	@Override
	public void upload(String url, BufferedImage image) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BufferedImage toImage(byte[] imageBuf) {
		// TODO Auto-generated method stub
		//???b??????????????????
		ByteArrayInputStream in = new ByteArrayInputStream(imageBuf);
		//???in????????????????????????????????????image???????????????in?????????ByteArrayInputStream();
		BufferedImage bufferdImage = null;
		try {
			bufferdImage = ImageIO.read(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bufferdImage;
	}
	
/////////////////
//https://www.cnblogs.com/passedbylove/p/11685538.html
/////////////////

	@Override
	public String encodeToString(BufferedImage image, String type) {
		// TODO Auto-generated method stub
		String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
            imageString = encoder.encode(imageBytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
	}

	@Override
	public BufferedImage decodeToImage(String imageString) {
		// TODO Auto-generated method stub
		BufferedImage image = null;
        byte[] imageByte;
        try {
        	sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
	}
	
/////////////////
//https://www.cnblogs.com/bevis-byf/p/11720968.html
/////////////////

	@Override
	public BufferedImage scale(BufferedImage image, float scale) {
		// TODO Auto-generated method stub
		int width = image.getWidth(); // ???????????????
        int height = image.getHeight(); // ???????????????
        width = (int) (width * scale);
        height = (int) (height * scale);
        Image scaleImage = image.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(scaleImage, 0, 0, null); // ?????????????????????
        g.dispose();
        return tag;
	}

	@Override
	public BufferedImage scale2(BufferedImage bi, int width, int height, boolean bb) {
		// TODO Auto-generated method stub
		double ratio = 0.0; // ????????????
        Image itemp = bi.getScaledInstance(width, height, bi.SCALE_SMOOTH);
        // ????????????
        if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
            if (bi.getHeight() > bi.getWidth()) {
                ratio = (new Integer(height)).doubleValue()
                        / bi.getHeight();
            } else {
                ratio = (new Integer(width)).doubleValue() / bi.getWidth();
            }
            AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(ratio, ratio), null);
            itemp = op.filter(bi, null);
        }
        if (bb) {//??????
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, width, height);
            if (width == itemp.getWidth(null))
                g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2,
                        itemp.getWidth(null), itemp.getHeight(null),
                        Color.white, null);
            else
                g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0,
                        itemp.getWidth(null), itemp.getHeight(null),
                        Color.white, null);
            g.dispose();
            itemp = image;
        }
        return (BufferedImage) itemp;
	}

	@Override
	public BufferedImage cut(BufferedImage bi, int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		int srcWidth = bi.getHeight(); // ????????????
        int srcHeight = bi.getWidth(); // ????????????
        if (srcWidth > 0 && srcHeight > 0) {
            Image image = bi.getScaledInstance(srcWidth, srcHeight,
                    Image.SCALE_DEFAULT);
            // ????????????????????????????????????????????????
            // ???: CropImageFilter(int x,int y,int width,int height)
            ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
            Image img = Toolkit.getDefaultToolkit().createImage(
                    new FilteredImageSource(image.getSource(), cropFilter));
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics();
            g.drawImage(img, 0, 0, width, height, null); // ?????????????????????
            g.dispose();
            return tag;
        }
        return null;
	}

	/*
	public BufferedImage cut2(BufferedImage bi, int rows, int cols) {
		// TODO Auto-generated method stub
		if(rows<=0||rows>20) rows = 2; // ????????????
        if(cols<=0||cols>20) cols = 2; // ????????????
        // ???????????????
        int srcWidth = bi.getHeight(); // ????????????
        int srcHeight = bi.getWidth(); // ????????????
        if (srcWidth > 0 && srcHeight > 0) {
            Image img;
            ImageFilter cropFilter;
            Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
            int destWidth = srcWidth; // ?????????????????????
            int destHeight = srcHeight; // ?????????????????????
            // ??????????????????????????????
            if (srcWidth % cols == 0) {
                destWidth = srcWidth / cols;
            } else {
                destWidth = (int) Math.floor(srcWidth / cols) + 1;
            }
            if (srcHeight % rows == 0) {
                destHeight = srcHeight / rows;
            } else {
                destHeight = (int) Math.floor(srcWidth / rows) + 1;
            }
            // ??????????????????
            // ???????????????:???????????????????????????????????????
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // ????????????????????????????????????????????????
                    // ???: CropImageFilter(int x,int y,int width,int height)
                    cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                            destWidth, destHeight);
                    img = Toolkit.getDefaultToolkit().createImage(
                            new FilteredImageSource(image.getSource(),
                                    cropFilter));
                    BufferedImage tag = new BufferedImage(destWidth,
                            destHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics g = tag.getGraphics();
                    g.drawImage(img, 0, 0, null); // ?????????????????????
                    g.dispose();
                    return tag;
                }
            }
        }
        return null;
	}

	@Override
	public BufferedImage cut3(BufferedImage bi, int destWidth, int destHeight) {
		// TODO Auto-generated method stub
		if(destWidth<=0) destWidth = 200; // ????????????
        if(destHeight<=0) destHeight = 150; // ????????????
        // ???????????????
        int srcWidth = bi.getHeight(); // ????????????
        int srcHeight = bi.getWidth(); // ????????????
        if (srcWidth > destWidth && srcHeight > destHeight) {
            Image img;
            ImageFilter cropFilter;
            Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
            int cols = 0; // ??????????????????
            int rows = 0; // ??????????????????
            // ????????????????????????????????????
            if (srcWidth % destWidth == 0) {
                cols = srcWidth / destWidth;
            } else {
                cols = (int) Math.floor(srcWidth / destWidth) + 1;
            }
            if (srcHeight % destHeight == 0) {
                rows = srcHeight / destHeight;
            } else {
                rows = (int) Math.floor(srcHeight / destHeight) + 1;
            }
            // ??????????????????
            // ???????????????:???????????????????????????????????????
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    // ????????????????????????????????????????????????
                    // ???: CropImageFilter(int x,int y,int width,int height)
                    cropFilter = new CropImageFilter(j * destWidth, i * destHeight,
                            destWidth, destHeight);
                    img = Toolkit.getDefaultToolkit().createImage(
                            new FilteredImageSource(image.getSource(),
                                    cropFilter));
                    BufferedImage tag = new BufferedImage(destWidth,
                            destHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics g = tag.getGraphics();
                    g.drawImage(img, 0, 0, null); // ?????????????????????
                    g.dispose();
                    return tag;
                }
            }
        }
        return null;
	}*/

	@Override
	public BufferedImage gray(BufferedImage image) {
		// TODO Auto-generated method stub
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        return op.filter(image, null);
	}
	
	/**
     * ??????text??????????????????????????????????????????
     * @param text
     * @return
     */
    private int getLength(String text) {
        int length = 0;
        for (int i = 0; i < text.length(); i++) {
            if (new String(text.charAt(i) + "").getBytes().length > 1) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length / 2;
    }

	@Override
	public BufferedImage pressText(BufferedImage src, String pressText, String fontName, int fontStyle, int color,
			int fontSize, int x, int y, float alpha) {
		// TODO Auto-generated method stub
		int width = src.getWidth(null);
        int height = src.getHeight(null);
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(src, 0, 0, width, height, null);
        g.setColor(ColorUtil.intToColor(color));
        g.setFont(new Font(fontName, fontStyle, fontSize));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                alpha));
        // ?????????????????????????????????
        g.drawString(pressText, (width - (getLength(pressText) * fontSize))
                / 2 + x, (height - fontSize) / 2 + y);
        g.dispose();
        return image;
	}

	@Override
	public BufferedImage pressText2(BufferedImage src, String pressText, String fontName, int fontStyle, int color,
			int fontSize, int x, int y, float alpha) {
		// TODO Auto-generated method stub
		int width = src.getWidth(null);
        int height = src.getHeight(null);
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(src, 0, 0, width, height, null);
        g.setColor(ColorUtil.intToColor(color));
        g.setFont(new Font(fontName, fontStyle, fontSize));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                alpha));
        // ?????????????????????????????????
        g.drawString(pressText, (width - (getLength(pressText) * fontSize))
                / 2 + x, (height - fontSize) / 2 + y);
        g.dispose();
        return image;
	}

	@Override
	public BufferedImage pressImage(BufferedImage src, BufferedImage src_biao, int x, int y, float alpha) {
		// TODO Auto-generated method stub
        int wideth = src.getWidth(null);
        int height = src.getHeight(null);
        BufferedImage image = new BufferedImage(wideth, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.drawImage(src, 0, 0, wideth, height, null);
        // ????????????
        int wideth_biao = src_biao.getWidth(null);
        int height_biao = src_biao.getHeight(null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,
                alpha));
        g.drawImage(src_biao, (wideth - wideth_biao) / 2,
                (height - height_biao) / 2, wideth_biao, height_biao, null);
        // ??????????????????
        g.dispose();
        return image;
	}
	
    /////////////////
    //https://www.jb51.net/article/60806.htm
    ////////////////

	@Override
	public BufferedImage rotateImage(BufferedImage inputImage, float degree) {
		// TODO Auto-generated method stub
		int w = inputImage.getWidth();
        int h = inputImage.getHeight();
        int type = inputImage.getColorModel().getTransparency();
        BufferedImage img=new BufferedImage(w, h, type);
        Graphics2D graphics2d =img.createGraphics();
        //???????????????
        RenderingHints renderingHints=new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_ANTIALIAS_ON);
        //?????????????????????
        renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2d.setRenderingHints(renderingHints);
        graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);
        graphics2d.drawImage(inputImage, 0, 0, null);
        graphics2d.dispose();
        return img;
	}

	@Override
	public BufferedImage flipHorizontalImage(BufferedImage inputImage) {
		// TODO Auto-generated method stub
		 int w = inputImage.getWidth();
		 int h = inputImage.getHeight();
	     BufferedImage img;
	     Graphics2D graphics2d;
		 (graphics2d = (img = new BufferedImage(w, h, inputImage
				 .getColorModel().getTransparency()))
				 .createGraphics())
		 .drawImage(inputImage, 0, 0, w, h, w, 0, 0, h, null);
		 graphics2d.dispose();
		 return img;
	}

	@Override
	public BufferedImage flipVerticalImage(BufferedImage inputImage) {
		// TODO Auto-generated method stub
		int w = inputImage.getWidth();
        int h = inputImage.getHeight();
        BufferedImage img;
        Graphics2D graphics2d;
        (graphics2d = (img = new BufferedImage(w, h, inputImage
                .getColorModel().getTransparency())).createGraphics())
                .drawImage(inputImage, 0, 0, w, h, 0, h, w, 0, null);
        graphics2d.dispose();
        return img;
	}
	
/////////////////////////
//https://blog.csdn.net/u012954706/article/details/83510028
/////////////////////////

	@Override
	public BufferedImage createQRImage(String text, int width, int height, int whiteSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage createLogoQRImage(String text, int width, int height, int whiteSize, BufferedImage logo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readQRImage(BufferedImage image) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferedImage reduceImg(BufferedImage image, int widthdist, int heightdist, Float rate) {
		// TODO Auto-generated method stub
		try {
			// ????????????????????????????????????????????????
            if (rate != null && rate > 0) {
                //???????????????????????????????????????
                int results[] = { 0, 0 };
                results[0] =image.getWidth(null); // ??????????????????
                results[1] =image.getHeight(null);// ??????????????????
                if (results == null || results[0] == 0 || results[1] == 0) {
                    return null;
                } else {
                    //???????????????????????????????????????????????????????????????
                    widthdist = (int) (results[0] * rate);
                    heightdist = (int) (results[1] * rate);
                }
            }
            // ?????????????????????????????????
            Image src = (Image) image;
            // ??????????????????????????????????????????????????? BufferedImage
            BufferedImage tag = new BufferedImage(
            		(int) widthdist, (int) heightdist, BufferedImage.TYPE_INT_RGB);
            //????????????  getScaledInstance?????????????????????????????????????????????????????????????????????Image,????????????width,height????????????
            //Image.SCALE_SMOOTH,?????????????????????????????????????????????????????????????????????????????????
            tag.getGraphics().drawImage(
            		src.getScaledInstance(widthdist, heightdist, Image.SCALE_SMOOTH), 0, 0, null);
            return tag;
        } catch (Exception ef) {
            ef.printStackTrace();
        }
        return  null;
	}

}
