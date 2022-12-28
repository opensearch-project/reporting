## Developer Guide

### Prerequisites

The node version "^12.20.0 || >=14" is required.

### SOP for installing and running reporting-cli

1. Checkout this package from version control
    ```
    git clone git@github.com:opensearch-project/dashboards-reporting.git
    cd dashboards-reporting
    git checkout main
    ```
2. Run `yarn` inside `reporting-cli/src`
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

### Running Tests

For runninng tests locally, setup opensearch and add sample data for tests execution.

Setup on linux:
```
sudo swapoff -a
sudo sysctl -w vm.swappiness=1
sudo sysctl -w fs.file-max=262144
sudo sysctl -w vm.max_map_count=262144
wget https://opensearch.org/samples/docker-compose.yml
docker-compose up --detach
curl -XPOST -u 'admin:admin' 'http://localhost:5601/api/sample_data/ecommerce' -H 'osd-xsrf:true'
curl -XPOST -u 'admin:admin' 'http://localhost:5601/api/sample_data/logs' -H 'osd-xsrf:true'  -H 'securitytenant: global'
curl -XPOST -u 'admin:admin' 'http://localhost:5601/api/sample_data/flights' -H 'osd-xsrf:true'  -H 'securitytenant: admin_tenant'
```
Run `yarn test` inside `reporting-cli`.