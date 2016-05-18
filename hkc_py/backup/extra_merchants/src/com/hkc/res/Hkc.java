/**
 * @author BLSM
 * @date Sat Aug 02 12:04:38 CST 2014
 */

package com.hkc.res;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;

import com.hkc.xml.utils.Attribute;
import com.hkc.xml.utils.DocumentUtils;
import com.hkc.xml.utils.ElementUtils;
import com.hkc.xml.utils.SAXReader;

public class Hkc {

	private HashMap<String, Integer> resHashMap = new HashMap<String, Integer>();

	private static Hkc uHkc;

	public Hkc(Context context) {
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(context.getAssets().open(
					ResGen.HKC_XMLFILE_NAME));
			Element rootElement = DocumentUtils.getInstance().getRootElement(
					document);
			List<Element> elements = ElementUtils.getInstance().elements(
					rootElement, "public");
			for (Element element : elements) {
				List<Attribute> attributes = ElementUtils.getInstance()
						.attributes(element);
				String key_type = "";
				String key_name = "";
				String value_id = "";
				for (Attribute attribute : attributes) {
					if ("type".equals(attribute.getName()))
						key_type = attribute.getValue();
					if ("name".equals(attribute.getName()))
						key_name = attribute.getValue();
					if ("id".equals(attribute.getName()))
						value_id = attribute.getValue();
				}
				resHashMap.put(key_type + "_" + key_name,
						Integer.parseInt(value_id.replace("0x", ""), 16));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int R(Context context, Rx res) {
		if (uHkc == null) {
			uHkc = new Hkc(context);
		}

		return uHkc.resHashMap.get(res.id_name);
	}

	public enum Rx {
				drawable_hkc_draw_splash_bg ("drawable_hkc_draw_splash_bg") ,
		drawable_hkc_ic_launcher ("drawable_hkc_ic_launcher") ,
		drawable_hkc_notify_icon ("drawable_hkc_notify_icon") ,
		id_hkc_image ("id_hkc_image") ,
		id_hkc_merchants_icon ("id_hkc_merchants_icon") ,
		id_hkc_merchants_subtitle ("id_hkc_merchants_subtitle") ,
		id_hkc_merchants_title ("id_hkc_merchants_title") ,
		id_hkc_merchants_webview ("id_hkc_merchants_webview") ,
		id_hkc_webview ("id_hkc_webview") ,
		layout_hkc_activity_hkcuninstall_moniter ("layout_hkc_activity_hkcuninstall_moniter") ,
		layout_hkc_activity_merchants ("layout_hkc_activity_merchants") ,
		layout_hkc_activity_splash ("layout_hkc_activity_splash") ,
		layout_hkc_layout_show_notify ("layout_hkc_layout_show_notify") ,
		layout_hkc_notification_merchants ("layout_hkc_notification_merchants") ,
		string_hkc_app_name ("string_hkc_app_name") ,
		string_hkc_copy_success ("string_hkc_copy_success") ,
		string_hkc_title_activity_uninstall_moniter ("string_hkc_title_activity_uninstall_moniter") ,

		Rx("Rx");
		public final String id_name;

		Rx(String id_name) {
			this.id_name = id_name;
		}
		
	}
}