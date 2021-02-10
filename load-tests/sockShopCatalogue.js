import http from "k6/http";
import { check, group, sleep, fail } from "k6";
import { Rate } from "k6/metrics";

if (__ENV["SERVICE_ADDRESS"] == null) {
    fail('SERVICE_ADDRESS argument is missing');
}

const failureRate = new Rate("failure_rate");
const url = `http://${__ENV["SERVICE_ADDRESS"]}`;

export let options = {
    stages: [
        { target: 1, duration: "1m" },
        { target: 50, duration: "1m30s" },
        { target: 50, duration: "2m" },
        { target: 1, duration: "1m30s" },
        { target: 1, duration: "1m" },
    ],
    ext: {
        loadimpact: {
            projectID: 3524085,
            name: 'sock-shop-catalogue-load-tests',
            distribution: {
                gb: { loadZone: "amazon:gb:london", percent: 50 },
                us: { loadZone: "amazon:us:portland", percent: 50 }
            }
        }
    }
};

function generateJsonBody(URL, responseJson) {
    const staticReqs = [];
    for (let index = 0; index < responseJson.length; index++) {
        const image1 = responseJson[index].imageUrl[0];
        const image2 = responseJson[index].imageUrl[1];
        const staticImage1 = ["GET", URL + image1, { tags: { staticAsset: "yes" } } ];
        const staticImage2 = ["GET", URL + image2, { tags: { staticAsset: "yes" } } ];
        staticReqs.push(staticImage1);
        staticReqs.push(staticImage2);
    }
    return staticReqs;
}

export default function() {
    let response = http.get(url + "/catalogue");
    //console.log('URL: ' + response.url + '\nDuration: ' + response.timings.duration + 'ms');
    const staticReqs = generateJsonBody(url, JSON.parse(response.body));
    let checkRes = check(response, {
        //"http2 is used": (r) => r.proto === "HTTP/2.0",
        "status is 200": (r) => r.status === 200,
        //"content is present": (r) => r.body.indexOf("Welcome to the LoadImpact.com demo site!") !== -1,
    });
    failureRate.add(!checkRes);

    // Load static assets, all requests
    group("Static Assets", function () {
        // Execute multiple requests in parallel like a browser, to fetch some static resources
        let resps = http.batch(
            staticReqs
        );
        
        // Combine check() call with failure tracking
        /*failureRate.add(!check(resps, {
            "status is 200": (r) => r[0].status === 200 && r[1].status === 200,
            "reused connection": (r) => r[0].timings.connecting == 0,
        })); */
    });

    sleep(Math.random() * 2 + 1.1); // Random sleep between 1.1s and 3.1s
}