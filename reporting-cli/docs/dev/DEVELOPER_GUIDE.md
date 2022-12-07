## Developer Guide



### SOP for installing and running reporting-cli

1. Checkout this package from version control
    ```
    git clone git@github.com:opensearch-project/dashboards-reporting.git
    cd dashboards-reporting
    git checkout main
    ```
2. Run `npm install` inside `reporting-cli/src`
3. You can run the below commands inside `reporting-cli/src`
    ```
    node index.js --url <url>
    ```
    For additional command line parameter options
    ```
    node index.js -h
    ```
4. Alternatively, you can use npm install to run this command from any directory.
    ```
    cd `reporting-cli/
    npm install -g .
    ```
    Once the installation is complete, you can use
    ```
    reporting --url <url> 
    ```

    To uninstall, use  
    ```
    npm uninstall -g reporting
    ```
    