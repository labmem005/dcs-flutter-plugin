
class Payload {

  final String text;
  final String type;

	Payload.fromJsonMap(Map<String, dynamic> map): 
		text = map["text"],
		type = map["type"];

	Map<String, dynamic> toJson() {
		final Map<String, dynamic> data = new Map<String, dynamic>();
		data['text'] = text;
		data['type'] = type;
		return data;
	}
}
