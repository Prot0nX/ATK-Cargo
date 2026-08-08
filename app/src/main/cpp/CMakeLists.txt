cmake_minimum_required(VERSION 3.4.1)

add_library(secrets SHARED
        secrets.cpp)

find_library(log-lib
        log)

target_link_libraries(secrets
        ${log-lib})

# سازگاری با اندروید ۱۵+ که اندازه صفحه حافظه ۱۶ کیلوبایتی است (بدون این، dlopen با
# UnsatisfiedLinkError روی چنین دستگاه‌هایی شکست می‌خورد)
target_link_options(secrets PRIVATE "-Wl,-z,max-page-size=16384")