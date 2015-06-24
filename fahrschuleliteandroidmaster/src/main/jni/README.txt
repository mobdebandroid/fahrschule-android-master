To build the libraries the first time, run ../scripts/build-native.sh
This will unarchive all necessary source code to build the PDF viewer and then build all JNI packages. 
Afterwards, you can just do ndk-build

When executing ndk-build a library file will be created:   

libpdfview2.so - The PDF viewer engine.
