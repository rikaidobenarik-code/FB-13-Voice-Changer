cmake_minimum_required(VERSION 3.10.2)

project("fb13voiceengine")

# إضافة مكتبة الـ C++ المسؤولة عن معالجة الصوت
add_library(
        fb13voiceengine
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        fb13voiceengine
        ${log-lib}
)
