import http from "k6/http";
import {check} from "k6";
import {Rate} from "k6/metrics";

export let options = {
    stages: [
        {target: 10, duration: "3m"},
        {target: 50, duration: "3m"},
        {target: 5, duration: "3m"},
    ],
    ext: {
        loadimpact: {
            projectID: 3524085,
            name: 'load-tests',
            distribution: {
                kr: {loadZone: "amazon:us:palo alto", percent: 100},
            }
        }
    }
};

const failureRate = new Rate("failure_rate");

const url = `http://${__ENV["SERVICE_ADDRESS"]}`;

export default function () {
    let response = http.get(url);
    let checkRes = check(response, {
        "status is 200": (r) => r.status === 200,
    });
    failureRate.add(!checkRes);
}