# Knowledge-Islands
This tool is used to check the concentration of knowledge in software projects. The tool uses a combination of shell scripts to extract data from projects, therefore a Linux environment is necessary.

## Prerequisites for use
### Back end
1. You have to install rugged and [github-linguist](https://github.com/github-linguist/linguist) gems in your system-wide ruby.<br/>
   On Ubuntu:<br/>
   `sudo apt-get install build-essential cmake pkg-config libicu-dev zlib1g-dev libcurl4-openssl-dev libssl-dev ruby-dev`<br/>
   `sudo /usr/bin/ruby -S gem install github-linguist`<br/>
   `sudo /usr/bin/ruby -S gem install rugged`
2. You have to install [Cloc](https://github.com/AlDanial/cloc#install-via-package-manager)<br/>
   `sudo apt install cloc`
### Front end
1. Ensure you have [Node.js](https://nodejs.org/en) installed, which includes npm (Node Package Manager).
2. Navigate to the 'front-end' directory and run `npm install` to install all necessary dependencies.
3. Use `npm start` to start the development server and `npm run build` to create a production build of the project. 

## Main endpoints
For now, the application's endpoints can be used locally to extract data and compute project truck factors.
The main endpoints to achieve these goals are:
1. '/api/project/generate-logs-folder/': this endpoint receives the root path of a .git repository, and generates .log files for computing the project's truck factor
2. '/api/project/repo-truck-factor/': this endpoint receives the root path of a .git repository with .log files already generated, and computes and saves data about the truck factor of that repository

In the same controllers are endpoints to perform the same tasks in folders with several repositories.
   
## References
<a id="1" href="https://dl.acm.org/doi/abs/10.1145/3544902.3546243">[1]</a> Cury, Otávio, et al. "Identifying Source Code File Experts." Proceedings of the 16th ACM/IEEE International Symposium on Empirical Software Engineering and Measurement. 2022.<br/>
<a id="2" href="https://www.sciencedirect.com/science/article/abs/pii/S0950584924000508">[2]</a> Cury, Otávio, et al. "Source code expert identification: Models and application." Information and Software Technology (2024): 107445.
<a id="3" href="https://arxiv.org/abs/2408.08733">[3]</a> Cury, Otávio, et al. "Knowledge Islands: Visualizing Developers Knowledge Concentration". Simpósio Brasileiro de Engenharia de Software (2024).
