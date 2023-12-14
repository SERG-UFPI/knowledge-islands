# Knowledge-Islands
This tool is used to check the concentration of knowledge in software projects. The tool uses a combination of shell scripts to extract data from projects.

## Prerequisites for use
1. You have to install rugged and [github-linguist](https://github.com/github-linguist/linguist) gems in your system-wide ruby.
   On Ubuntu:
   `sudo apt-get install build-essential cmake pkg-config libicu-dev zlib1g-dev libcurl4-openssl-dev libssl-dev ruby-dev`
   `sudo /usr/bin/ruby -S gem install github-linguist`
   `sudo /usr/bin/ruby -S gem install rugged`
3. You have to install [Cloc](https://github.com/AlDanial/cloc#install-via-package-manager)
   `sudo apt install cloc`
