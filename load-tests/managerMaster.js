import http from 'k6/http';
import { check } from "k6";
import encoding from "k6/encoding";

export let options = {
    vus: '10',
    iterations: '10',
};

const url = `http://${__ENV["HOST_ADDRESS"]}:8080/api/${__ENV["URL"]}`;
const params = {
    headers: {
        "Authorization": `Basic ${encoding.b64encode("admin:admin")}`,
        "Content-Type": "application/json"
    },
    timeout: "300s"
};
export default function () {
    const method = __ENV["METHOD"].toUpperCase();
    let response = http.request(method, url, __ENV["REQUEST_BODY"], params);
    check(response, {
        "status is 200": (r) => r.status === 200,
    });
}
