import http from "k6/http";
import {check, sleep, fail} from "k6";
import {Rate} from "k6/metrics";
import encoding from "k6/encoding";

if (__ENV["SERVICE_ADDRESS"] == null) {
    fail('SERVICE_ADDRESS argument is missing');
}

const failureRate = new Rate("failure_rate");
const url = `http://${__ENV["SERVICE_ADDRESS"]}`;

export let options = {
    stages: [
        { target: 1, duration: "1m" },
        { target: 100, duration: "1m30s" },
        { target: 100, duration: "2m" },
        { target: 1, duration: "1m30s" },
        { target: 1, duration: "1m" },
    ],
    ext: {
        loadimpact: {
            projectID: 3524085,
            name: 'sock-shop-user-load-tests',
            distribution: {
                gb: { loadZone: "amazon:gb:london", percent: 50 },
                us: { loadZone: "amazon:us:portland", percent: 50 }
            }
        }
    }
};
function generateWord(isOnlyLetters, size) {
    const letters = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "x", "z"];
    const numbers = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"];
    let word = "";
    for (let i = 0; i < size; i++) {
        let isLetter = isOnlyLetters || Math.random() < 0.5;
        if (isLetter) {
            let randomLetter = Math.floor(Math.random() * letters.length);
            word += letters [randomLetter];
        } else {
            let randomNumber = Math.floor(Math.random() * numbers.length);
            word += numbers [randomNumber];
        }
    }
    return word;

}
function generateRegisterPayload() {
    const username = generateWord(false, 8);
    const password = generateWord(false, 4);
    const email = generateWord(false, 8) + "@email." + generateWord(true, 3);
    const firstName = generateWord(true, 6);

    const lastName = generateWord(true, 6);
    return JSON.stringify({
        "username": username,
        "password": password,
        "email": email,
        "firstName": firstName,
        "lastName": lastName
    });

}
function getRandomUser() {

    const users = [
        {username: "Eve_Berger", password: "eve"},
        {username: "user", password: "password"},
        {username: "user1", password: "password"}
    ];
    const randomUser = Math.floor(Math.random() * users.length);

    const userEncoded = encoding.b64encode(users[randomUser].username + ":" + users[randomUser].password);
    return "Basic " + userEncoded;
}

export default function() {
    let response;
    if (Math.random() < 0.5) {
        const authUser = getRandomUser();
        const params = {headers: {"Authorization": authUser, "Content-Type": "application/json"}};
        response = http.get(url + "/login", params);
    } else {
        const payload = generateRegisterPayload();
        const params = {headers: {"Content-Type": "application/json"}};
        response = http.post(url + "/register", payload, params);
    }
    let checkRes = check(response, {
        "status is 200": (r) => r.status === 200,
    });
    failureRate.add(!checkRes);
    sleep(Math.random() + 0.1); // Random sleep between 0s and 1s
}