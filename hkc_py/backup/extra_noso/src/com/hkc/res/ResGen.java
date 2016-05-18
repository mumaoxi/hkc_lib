package com.hkc.res;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Date;

import com.hkc.R;
import com.hkc.utils.ResourceUtils;


public class ResGen {

	private static final String TAG = ResGen.class.getSimpleName();

	private static final String CLASS_NAME_PREFIX = "SS";
	private static final String FILE_NAME = CLASS_NAME_PREFIX + ".java";
	private static final String CURRENT_PACKAGE = "com.hkc";
	private static final String HKC_PACKAGE_NAME = "com.hkc";
	private static final String IMPORT_RES_NAME = HKC_PACKAGE_NAME + ".res";
	private static final String AUTHOR = "BLSM";
	private static final String ID_PREFIX = "hkc_";
	public static final String HKC_XMLFILE_NAME = ID_PREFIX + "res.xml";
	private static final String HKC_FILE_NAME = "Hkc";

	private static final String R_NAME_PREFIX = "Res";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Generate R
		generateResXml(CURRENT_PACKAGE);
		generateHkc(IMPORT_RES_NAME);
	}

	/**
	 * 自动生成 Res.java文件,这个文件与R.java的资源id一一对应
	 * 
	 * @param packageName
	 * @param fileName
	 */
	@SuppressWarnings("unchecked")
	public static void generateR(String packageName, String fileName) {

		if (packageName == null) {
			ResLog.e(TAG, "Package name is null, somethings wrong!!!");
			return;
		}

		if (fileName == null) {
			ResLog.e(TAG, "File name is null, something wrong!!!");
			return;
		}

		StringBuffer content = new StringBuffer();
		// Add file comment.
		content.append(getHeaderJavadoc());
		// Add file package and import.
		content.append("package " + packageName + ";\r\n\r\n");
		content.append("import " + HKC_PACKAGE_NAME + ".R;\r\n\r\n");
		content.append("public class " + fileName + " {\r\n" + "\r\n");
		try {
			Class<R> clz = (Class<R>) Class.forName(HKC_PACKAGE_NAME + ".R");
			Class<?>[] clzs = clz.getClasses();
			for (Class<?> innerClz : clzs) {
				/**
				 * public enum anim {
				 * 
				 * fresh_alpha_in("fresh_alpha_in"); private String name;
				 * 
				 * // 构造方法 private anim(String name) { this.name = name; }
				 * 
				 * public String getName() { return name; } }
				 */
				String className = innerClz.getSimpleName();
				// Create enum.
				StringBuffer classContent = new StringBuffer();
				classContent.append("\n\n\t" + " public static class  "
						+ className + " {  \n ");
				Field[] fields = innerClz.getFields();
				int fieldsCount = 0;
				for (Field field : fields) {
					field.setAccessible(true);
					String fieldName = field.getName();

					// 只解析hkc开始的文件或资源id
					if (fieldName.startsWith(ID_PREFIX)) {
						fieldsCount++;
						if ("class [I".equals(field.getType().toString())) {
							classContent.append("\n\t\tpublic static int[] "
									+ fieldName + " = R." + className + "."
									+ fieldName + ";");
						} else {
							classContent.append("\n\t\tpublic static int "
									+ fieldName + " = R." + className + "."
									+ fieldName + ";");
						}
					}

				}// End for fields
				classContent.append("\n\n\t}");

				if (fieldsCount > 0) {
					content.append(classContent);
				}

			}// End for classes
			content.append("}");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		StringBuffer filePath = new StringBuffer("src/");
		packageName = packageName.replace(".", "/");
		filePath = filePath.append(packageName);
		filePath = filePath.append("/" + fileName + ".java");
		File oldFile = new File(filePath.toString());
		// Delete old file.
		if (oldFile.exists()) {
			boolean result = oldFile.delete();
			if (result) {
				ResLog.o(TAG, "Delete old " + fileName + " success!!!");
			} else {
				ResLog.e(TAG, "Delete old " + fileName + "failed!!!");
				return;
			}
		}

		// Create new file.
		File newFile = new File(filePath.toString());
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(newFile));
				os.write(content.toString().getBytes());
				os.flush();
				os.close();
				ResLog.o(TAG, "Auto generate " + FILE_NAME + " ok!!!");
			} catch (IOException e) {
				ResLog.t(e);
				ResLog.e(TAG, "Auto generate " + FILE_NAME + " failed!!!");
			}
		}
	}

	/**
	 * 自动生成hkc_res.xml
	 * 
	 * @param packageName
	 * @param fileName
	 */
	public static void generateResXml(String packageName) {

		if (packageName == null) {
			ResLog.e(TAG, "Package name is null, somethings wrong!!!");
			return;
		}

		StringBuffer xmlContent = new StringBuffer();
		xmlContent.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n");
		xmlContent.append("<resources>\r\n");
		try {
			Class<R> clz = (Class<R>) Class.forName(CURRENT_PACKAGE + ".R");
			Class<?>[] clzs = clz.getClasses();
			for (Class<?> innerClz : clzs) {
				xmlContent.append("\r\n");
				// attr、drawable等
				String className = innerClz.getSimpleName();
				Field[] fields = innerClz.getFields();
				for (Field field : fields) {
					field.setAccessible(true);
					String fieldName = field.getName();
					// 只解析hkc开始的文件或资源id
					if (fieldName.startsWith(ID_PREFIX)) {
						int resouceId = ResourceUtils.getResourceId(
								packageName, className, fieldName);
						xmlContent.append("\t<public type=\"" + className
								+ "\" name=\"" + fieldName + "\" id=\"0x"
								+ Integer.toHexString(resouceId) + "\" />\r\n");
					}

				}// End for fields
			}// End for classes

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		xmlContent.append("</resources>");

		StringBuffer filePath = new StringBuffer("assets/");
		filePath = filePath.append(HKC_XMLFILE_NAME);
		File oldFile = new File(filePath.toString());
		// Delete old file.
		if (oldFile.exists()) {
			boolean result = oldFile.delete();
			if (result) {
				ResLog.o(TAG, "Delete old " + filePath + " success!!!");
			} else {
				ResLog.e(TAG, "Delete old " + filePath + "failed!!!");
				return;
			}
		}

		// Create new file.
		File newFile = new File(filePath.toString());
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(newFile));
				os.write(xmlContent.toString().getBytes());
				os.flush();
				os.close();
				ResLog.o(TAG, "Auto generate " + filePath + " ok!!!");
			} catch (IOException e) {
				ResLog.t(e);
				ResLog.e(TAG, "Auto generate " + filePath + " failed!!!");
			}
		}
	}

	/**
	 * 自动生成Hkc文件
	 * 
	 * @param packageName
	 * @param fileName
	 */
	@SuppressWarnings("unchecked")
	private static void generateHkc(String packageName) {

		if (packageName == null) {
			ResLog.e(TAG, "Package name is null, somethings wrong!!!");
			return;
		}

		String content =readTmpJava();
		try {
			Class<R> clz = (Class<R>) Class.forName(CURRENT_PACKAGE + ".R");
			Class<?>[] clzs = clz.getClasses();

			/**
			 * Enum类的内容
			 */
			StringBuffer rxEnumContent = new StringBuffer();

			for (Class<?> innerClz : clzs) {
				String className = innerClz.getSimpleName();

				Field[] fields = innerClz.getFields();
				for (Field field : fields) {
					field.setAccessible(true);
					String fieldName = field.getName();

					// 只解析hkc开始的文件或资源id
					if (fieldName.startsWith(ID_PREFIX)) {
						rxEnumContent.append("\t\t" + className + "_"
								+ fieldName + " (\"" + className + "_"
								+ fieldName + "\") ,\r\n");
					}

				}// End for fields
			}
			
			content = String.format(content, packageName,rxEnumContent);

		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}

		StringBuffer filePath = new StringBuffer("src/");
		packageName = packageName.replace(".", "/");
		filePath = filePath.append(packageName);
		filePath = filePath.append("/" + HKC_FILE_NAME + ".java");
		File oldFile = new File(filePath.toString());
		// Delete old file.
		if (oldFile.exists()) {
			boolean result = oldFile.delete();
			if (result) {
				ResLog.o(TAG, "Delete old " + HKC_FILE_NAME + " success!!!");
			} else {
				ResLog.e(TAG, "Delete old " + HKC_FILE_NAME + "failed!!!");
				return;
			}
		}

		// Create new file.
		ResLog.o(TAG, "create new file:"+filePath.toString());
		File newFile = new File(filePath.toString());
		if (!newFile.exists()) {
			try {
				newFile.createNewFile();
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(newFile));
				os.write(content.toString().getBytes());
				os.flush();
				os.close();
				ResLog.o(TAG, "Auto generate " + HKC_FILE_NAME + " ok!!!");
			} catch (IOException e) {
				ResLog.t(e);
				ResLog.e(TAG, "Auto generate " + HKC_FILE_NAME + " failed!!!");
			}
		}
	}

	/**
	 * 读取java模板文件
	 * 
	 * @return
	 */
	private static String readTmpJava() {
		try {
			File file = new File("assets/code_for_tmp_hkc.hkc");
			InputStream inputStream = new FileInputStream(file);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			int len = 1024;
			byte[] bytes = new byte[len];
			while ((len = inputStream.read(bytes))>0) {
				outputStream.write(bytes, 0, len);
			}
			inputStream.close();
			return new String(outputStream.toByteArray(),"utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Generate CLASS_NAME_PREFIX.java header comment
	 * 
	 * @return
	 */
	private static String getHeaderJavadoc() {
		StringBuffer comment = new StringBuffer();
		comment.append("/**\r\n");
		comment.append(" * @author " + AUTHOR + "\r\n");
		comment.append(" * @date " + new Date() + "\r\n");
		comment.append(" */\r\n\r\n");
		return comment.toString();
	}

}
