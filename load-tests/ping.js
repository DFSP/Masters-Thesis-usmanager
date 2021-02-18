import http from 'k6/http';
import { check } from "k6";

export let options = {
    iterations: '10000',
    vus: '10'
};

export default function () {
    const url = `http://${__ENV["SERVICE_ADDRESS"]}`;
    const response = http.get(url);
    check(response, {
        "status is 200": (r) => r.status === 200,
    });
}
