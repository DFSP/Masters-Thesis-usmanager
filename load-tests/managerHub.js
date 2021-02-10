import http from "k6/http";
import {check} from "k6";
import {Rate} from "k6/metrics";

export let options = {
    duration: '5s',
};

const failureRate = new Rate("failure_rate");
const url = `http://localhost:3000`;

export default function() {
    let response = http.get(url);
    let checkRes = check(response, {
        "status is 200": (r) => r.status === 200,
    });
    failureRate.add(!checkRes);
}