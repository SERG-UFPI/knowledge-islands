# Knowledge-Islands
This tool is used to check the concentration of knowledge in software projects. The tool uses a combination of shell scripts to extract data from projects.

## Prerequisites for use
1. You have to install rugged and [github-linguist](https://github.com/github-linguist/linguist) gems in your system-wide ruby.<br/>
   On Ubuntu:<br/>
   `sudo apt-get install build-essential cmake pkg-config libicu-dev zlib1g-dev libcurl4-openssl-dev libssl-dev ruby-dev`<br/>
   `sudo /usr/bin/ruby -S gem install github-linguist`<br/>
   `sudo /usr/bin/ruby -S gem install rugged`
2. You have to install [Cloc](https://github.com/AlDanial/cloc#install-via-package-manager)<br/>
   `sudo apt install cloc`

## Main endpoints
For now, the application's endpoints can be used locally to extract data and compute project truck factors.
The main endpoints to achieve these goals are:
1. '/api/project/generate-logs-folder/': this endpoint receives the root path of a .git repository, and generates .log files for computing the project's truck factor
2. '/api/project/repo-truck-factor/': this endpoint receives the root path of a .git repository with .log files already generated, and computes and saves data about the truck factor of that repository

In the same controllers are endpoints to perform the same tasks in folders with several repositories.
   
## References
<a id="1">[1]</a> Cury, Otávio, et al. "Identifying Source Code File Experts." Proceedings of the 16th ACM/IEEE International Symposium on Empirical Software Engineering and Measurement. 2022.<br/>
<a id="2">[2]</a> Cury, Otávio, et al. "Source code expert identification: Models and application." Information and Software Technology (2024): 107445.
