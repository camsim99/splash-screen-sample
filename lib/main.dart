import 'package:flutter/material.dart';

Future<void> main() async {
  runApp(MyApp());
}

// Main app that contains the Flutter starter app
class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          children: <Widget>[
            // CustomAppBar(),
            SizedBox(height: 42),
            Align(alignment: Alignment.topCenter, child: CustomAppBar()),
            SizedBox(height: 250),
            Text(
              'You have pushed the button this many times:',
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _incrementCounter,
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}

// A Flutter implementation of the last frame of the splashscreen animation
// that will display at the top of the Flutter starter app upon it loading
class CustomAppBar extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    Widget titleSection = Row(
      children: [
        // SizedBox(width: 12),
        Padding(
          padding: EdgeInsets.only(left: 12, right: 4),
          child: ClipRRect(
              borderRadius: BorderRadius.circular(36.0),
              child: Image.asset(
                'images/androidIcon6.png',
                width: 72.0,
                height: 72.0,
                fit: BoxFit.fill,
              )),
        ),
        Padding(
          padding: EdgeInsets.only(top: 3),
          child: Text("Super Splash Screen Demo",
              style: TextStyle(color: Colors.black54, fontSize: 24)),
        ),
      ],
    );
    return titleSection;
  }
}
