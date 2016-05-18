package com.hkc.http;


public interface Constant {

	// 支付宝快捷支付,rsa 签名算法url
	public static final String ALIPAY_RSA = "http://paybank.sinaapp.com/index.php/Home/AlipayQuick/rsa";

	// 查看订单支付记录的api
	public static final String PAY_RECORD_URL = "http://paybank.sinaapp.com/index.php/home/pay_record/logs/order_num/%1$s";

	// Release server.
	public static final String SERVER_ROOT = "http://api.aihuo360.com/";
	// Debug server
	// public static final String SERVER_ROOT = "http://115.29.4.146:8080/";
	// Jiaopeng sever
	// public static final String SERVER_ROOT = "http://10.0.1.50:9393/";

	/**
	 * API，URL
	 */

	// Release secret server.
	public static final String SECRET_SERVER_ROOT = "http://api.aihuo360.com/v2";
	// Debug secret server.
	// public static final String SECRET_SERVER_ROOT = "http://115.29.4.146/v2";
	// Jiaopeng secret server
	// public static final String SECRET_SERVER_ROOT = "http://10.0.1.50/v2";

	public static final String NODE_ID = "168c7e73310557a8dba242b88184e585";

	// 创建设备
	public static final String API_CREATE_DEVICE = "/devices";

	/**
	 * 创建消息
	 */
	public static final String API_CREATE_MESSAGE = "/messages";

	/**
	 * 消息列表
	 */
	public static final String API_MESSAGES = "/messages";

	/**
	 * 首页，bannner+分类
	 */
	public static final String API_HOME = "/home";

	/**
	 * 商品列表
	 */
	public static final String API_PRODUCTS = "/products";

	/**
	 * 获取产品的二级分类
	 */
	public static final String API_PRODUCTS_CATEGORY = "/tags";

	/**
	 * 商品详细
	 */
	public static final String API_PRODUCT = "/products/%1$s";

	/**
	 * 商品的交易记录
	 */
	public static final String API_PRODUCT_TRADES = "/products/%1$s/trades";

	/**
	 * 优惠券
	 */
	public static final String API_COUPONS = "/coupons";
	/**
	 * 创建、更新购物车
	 */
	public static final String API_CARTS = "/carts";

	/**
	 * 订单
	 */
	public static final String API_ORDERS = "/orders";
	public static final String WEB_ORDER = "/orders/%1$s/edit_v2";
	public static final String API_ORDERS_UPDATE_ADDR = "/orders/%1$s/update_address";

	/**
	 * 订单详情
	 */
	public static final String API_ORDER_DETAIL = "/orders/%1$s";

	/**
	 * 获取文章列表 GET
	 */
	public static final String API_ARTICLES = "/articles";

	/**
	 * 获取文章详情 GET
	 */
	public static final String API_ARTICLE = "/articles/%1$s";

	/**
	 * 社区
	 */
	// 社区.板块列表
	public static final String API_NODES = "/nodes";
	// 社区.某个节点下的主题列表
	public static final String API_NODE_TOPIC_LIST = "/nodes/%1$s/topics";
	// 社区.某一类别帖子列表
	public static final String API_TOPIC_LIST = "/topics";
	// 社区.帖子详情
	public static final String API_TOPIC_DETAIL = "/topics/%1$s";
	// 社区.创建帖子
	public static final String API_TOPIC_CREATE = "/nodes/%1$s/topics";
	// 社区.帖子评论列表
	public static final String API_TOPIC_REPLIE_LIST = "/topics/%1$s/replies";
	// 社区，回复我的列表
	public static final String API_TOPIC_REPLIE_ME_LIST = "/replies";
	// 社区.回复帖子
	public static final String API_TOPIC_REPLIY_OF_TOPIC = "/topics/%1$s/replies";
	// 社区.二级回复
	public static final String API_TOPIC_REPLIY_OF_REPLIY = "/replies/%1$s";
	// 社区.喜欢某个帖子
	public static final String API_TOPIC_LIKE = "/topics/%1$s/like";
	// 社区.不喜欢某个帖子
	public static final String API_TOPIC_DISLIKE = "/topics/%1$s/dislike";
	// 社区.关注（收藏）某个帖子
	public static final String API_TOPIC_FOLLOW = "/topics/%1$s/follow";
	// 社区.取消关注（收藏）某个帖子
	public static final String API_TOPIC_UNFOLLOW = "/topics/%1$s/unfollow";
	// 社区.批量删除我发表的主题
	public static final String API_TOPIC_MULT_DELETE = "/topics";
	// 社区.批量删除我的喜欢
	public static final String API_TOPIC_FOLLOW_MULT_DELETE = "/topics/unfollow";

	// Members
	public static final String API_MEMBERS = "/members";
	public static final String API_MEMBERS_UPDATE = "/members/%1$s";
	public static final String API_MEMBERS_GET_CAPTCHA = "/members/%1$s/send_captcha";
	public static final String API_MEMBERS_VALIDATE = "/members/%1$s/validate_captcha";

	// private message
	public static final String API_PRIVATE_MSG = "/private_messages";
	public static final String API_PRIVATE_MSG_HISTORY = "/private_messages/history";
	public static final String API_PRIVATE_MSG_OPEN = "/private_messages/%1$s";
	/**
	 * 获取最热搜索关键词
	 */
	public static final String API_HOT_SEACH_WORDS = "/tags";

	/**
	 * 获取当前所在的城市
	 */
	public static final String API_CURRENT_LOCATION = "http://gadget.sinaapp.com/Common/Location/getCurrentLocation";

	/**
	 * 物流信息
	 */
	public static final String API_EXPRESS_INFO = "http://api.ickd.cn/?id=F15A96535B700B443183D4ABDC3AD993&type=json&encode=utf8";

	/**
	 * 更新百度云平台信息
	 */
	public static final String API_CREATE_BAIDU_DEVICE_INFO = "/device_infos";

	/*
	 * channel switch
	 */
	public static final String UMENG_ONLINE_PARAM_KEY_CHANNEL_OPEN = "channel_open";


	// Contents list
	public static final String API_CONTENTS_LIST = "/contents";
	public static final String API_CONTENTS_DETAIL = "/contents/%1$s";
	public static final String API_CONTENTS_LIKE = "/contents/%1$s/like";
	public static final String API_CONTENTS_DISLIKE = "/contents/%1$s/dislike";
	public static final String API_CONTENTS_FORWARD = "/contents/%1$s/forward";
	public static final String API_CONTENTS_REPLIES = "/contents/%1$s/replies";
	public static final String API_CONTENTS_REPLIE_OF_CONTENT = "/contents/%1$s/replies";
}
