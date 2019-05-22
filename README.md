# dcs-flutter-plugin

Used to integrate Baidu DCS Android SDK.

### Use this package as a library
#### 1. Depend on it
Add this to your package's pubspec.yaml file:
```
dependencies:
  fludcs: 
    path: ../dcs-flutter-plugin

```
#### 2. Install it
You can install packages from the command line:

with Flutter:

```
$ flutter packages get
```

#### 3. Import it
Now in your Dart code, you can use:
```
import 'package:fludcs/fludcs.dart' as fludcs;
```
### Example
```
import 'package:fludcs/fludcs.dart' as fludcs;

...

@override
  void initState() {
    super.initState();
    //Init DCS & Login
    fludcs.initDcs();
    fludcs.dcsAuth();
  }
  
...
```
### Testing
You can populate dcs-flutter-plugin with initial values in your tests by running this code:
```
const MethodChannel('com.roam2free/fludcs')
  .setMockMethodCallHandler((MethodCall methodCall) async {
    if (methodCall.method == 'getAll') {
      return <String, dynamic>{};
    }
    return null;
  });
```
