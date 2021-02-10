import http from "k6/http";
import {check, fail} from "k6";
import {Rate} from "k6/metrics";
import encoding from "k6/encoding";

if (__ENV["URL"] == null) {
    fail('URL argument is missing');
}
if (__ENV["METHOD"] == null) {
    fail('METHOD argument is missing');
}
if (__ENV["METHOD"] === 'post' || __ENV["METHOD"] === 'put') {
    if (__ENV["REQUEST_BODY"] == null) {
        fail('REQUEST_BODY argument is missing');
    }
}

export let options = {
    iterations: __ENV["ITERATIONS"] == null ? 1 : __ENV["ITERATIONS"],
};

const failureRate = new Rate("failure_rate");
const url = `http://${__ENV["HOST_ADDRESS"] == null ? "localhost" : __ENV["HOST_ADDRESS"]}:8080/api/${__ENV["URL"]}`;
const params = {
    headers: {
        "Authorization": `Basic ${encoding.b64encode("admin:admin")}`,
        "Content-Type": "application/json"
    },
    timeout: "300s"
};

export default function() {
    const method = __ENV["METHOD"].toUpperCase();
    let response = http.request(method, url, __ENV["REQUEST_BODY"], params);
    console.log(`${method} ${url} ${__ENV["REQUEST_BODY"]}`)
    console.log(`Response status: ${response.status}`)
    //console.log(`Duration: ${response.timings.duration} ms`)
    let checkRes = check(response, {
        "status is 200": (r) => r.status === 200,
        "duration is >= 0": (r) => r.timings.duration >= 0,
    });
    failureRate.add(!checkRes);
}

