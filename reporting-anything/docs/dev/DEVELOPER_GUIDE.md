## Developer Guide



### SOP for installing and running reporting-anything

1. Checkout this package from version control
    ```
    git clone git@github.com:rupal-bq/dashboards-reports.git
    cd dashboard-reports
    git checkout reporting-anything
    ```
2. Run `yarn` inside `dashboard-reports/reporting-anything/src`
3. You can run the below commands inside `dashboard-reports/reporting-anything/src`
    ```
    node download_reports.js --url <url>
    ```
    For additional command line parameter options
    ```
    node download_reports.js -h
    ```
4. Alternatively, you can use npm install to run this command from any directory.
    ```
    cd `dashboard-reports/reporting-anything/
    npm install -g .
    ```
    Once the installation is complete, you can use
    ```
    reporting-anything --url <url> 
    ```

    To uninstall, use  
    ```
    npm uninstall -g reporting-anything
    ```
    