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

function insert_card(object) {
    print(db.cards.insert(object));
}

insert_card({
    "_id": ObjectId("57a98d98e4b00679b4a830ae"),
    "longNum": "5953580604169678",
    "expires": "08/19",
    "ccv": "678"
});
insert_card({
    "_id": ObjectId("57a98d98e4b00679b4a830b1"),
    "longNum": "5544154011345918",
    "expires": "08/19",
    "ccv": "958"
});
insert_card({
    "_id": ObjectId("57a98d98e4b00679b4a830b4"),
    "longNum": "0908415193175205",
    "expires": "08/19",
    "ccv": "280"
});
insert_card({
    "_id": ObjectId("57a98ddce4b00679b4a830d2"),
    "longNum": "5429804235432",
    "expires": "04/16",
    "ccv": "432"
});

print("________CARD DATA_______");
db.cards.find().forEach(get_results);
print("______END CARD DATA_____");


