cmake_minimum_required(VERSION 3.4.1)

add_library(secrets SHARED
        secrets.cpp)

find_library(log-lib
        log)

target_link_libraries(secrets
        ${log-lib})

# تنظیم اندازه صفحه حافظه ۱۶ کیلوبایتی جهت سازگاری با اندروید ۱۵ و بالاتر.
target_link_options(secrets PRIVATE "-Wl,-z,max-page-size=16384")
