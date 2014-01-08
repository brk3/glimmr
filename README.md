![Header](https://raw.github.com/brk3/glimmr/master/res/drawable-xxxhdpi/ic_launcher.png)

Glimmr
======
Glimmr is a popular and fully working Flickr app for Android.

Building
========
You need to populate `src/main/java/com/bourke/glimmr/common/Keys.java` with a
[Flickr API Key](http://www.flickr.com/services/api/misc.api_keys.html).

Then run:
```bash
scripts/populate_local_repo.sh
mvn clean install
```

Contributing
============
Pull requests are of course welcome, all I ask is that you follow the existing
style of the code and as set out by
http://source.android.com/source/code-style.html

## License

    Copyright 2014 Paul Bourke

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
