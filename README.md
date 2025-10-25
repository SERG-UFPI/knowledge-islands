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
The application's endpoints can be used locally to extract data and compute project truck factors.
The main endpoints to achieve these goals are:
1. '/api/downloader/clone-repository': Clones a public repository into the folder specified by the 'configuration.permanent-clone.path' property in the 'application.properties' file.
2. '/api/git-repository/generate-logs-repository/': Accepts the root path of a .git repository and generates .log files necessary for calculating the project's Truck Factor.
3. '/api/truck-factor/save-full-truck-factor/': Takes the root path of a .git repository with pre-generated .log files and calculates and saves the repository's Truck Factor.
4. '/api/git-repository-version-process/start-git-repository-version-process/': Combines the functionalities of the previous endpoints: cloning the repository, generating log files, and calculating and saving the Truck Factor.

The same controllers also include endpoints for performing these tasks on folders containing multiple repositories.
