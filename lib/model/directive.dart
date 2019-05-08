import 'package:fludcs/model/header.dart';
import 'package:fludcs/model/payload.dart';

class Directive {

  final Header header;
  final Payload payload;

	Directive.fromJsonMap(Map<String, dynamic> map): 
		header = Header.fromJsonMap(map["header"]),
		payload = Payload.fromJsonMap(map["payload"]);

	Map<String, dynamic> toJson() {
		final Map<String, dynamic> data = new Map<String, dynamic>();
		data['header'] = header == null ? null : header.toJson();
		data['payload'] = payload == null ? null : payload.toJson();
		return data;
	}
	
	
}
