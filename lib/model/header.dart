
class Header {

  final String name;
  final String namespace;
  final String messageId;
  final String dialogRequestId;

	Header.fromJsonMap(Map<String, dynamic> map): 
		name = map["name"],
		namespace = map["namespace"],
		messageId = map["messageId"],
		dialogRequestId = map["dialogRequestId"];

	Map<String, dynamic> toJson() {
		final Map<String, dynamic> data = new Map<String, dynamic>();
		data['name'] = name;
		data['namespace'] = namespace;
		data['messageId'] = messageId;
		data['dialogRequestId'] = dialogRequestId;
		return data;
	}
}
