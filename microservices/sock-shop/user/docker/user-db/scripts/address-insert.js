/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

function get_results(result) {
    print(tojson(result));
}

function insert_address(object) {
    print(db.addresses.insert(object));
}

insert_address({
    "_id": ObjectId("57a98d98e4b00679b4a830ad"),
    "number": "246",
    "street": "Whitelees Road",
    "city": "Glasgow",
    "postcode": "G67 3DL",
    "country": "United Kingdom"
});
insert_address({
    "_id": ObjectId("57a98d98e4b00679b4a830b0"),
    "number": "246",
    "street": "Whitelees Road",
    "city": "Glasgow",
    "postcode": "G67 3DL",
    "country": "United Kingdom"
});
insert_address({
    "_id": ObjectId("57a98d98e4b00679b4a830b3"),
    "number": "4",
    "street": "Maes-Y-Deri",
    "city": "Aberdare",
    "postcode": "CF44 6TF",
    "country": "United Kingdom"
});
insert_address({
    "_id": ObjectId("57a98ddce4b00679b4a830d1"),
    "number": "3",
    "street": "my road",
    "city": "London",
    "country": "UK"
});

print("________ADDRESS DATA_______");
db.addresses.find().forEach(get_results);
print("______END ADDRESS DATA_____");
