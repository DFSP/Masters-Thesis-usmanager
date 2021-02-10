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

import React, {useEffect, useState} from 'react';

//import shuffle from 'lodash/shuffle'

interface AnimatedListProps<T> {
    list: T[];
    show: (element: T, index: number, last: boolean, list: T[]) => JSX.Element;
    header?: () => JSX.Element;
}

export default function AnimatedList<T>(props: AnimatedListProps<T>) {
    const {list, show, header} = props;
    const keyedList = list.map((item, index) => ({key: index, item}));
    const [rows, setList] = useState(keyedList);

    useEffect(() => setList(keyedList), [list, keyedList]);
    /*useEffect(() => void setInterval(() => setList(shuffle), 2000), [])*/

    let height = 0;
    /* const transitions = useTransition(
       rows.map(data => ({data, height: 32.4, y: (height += 32.4) - 32.4})),
       (item: any) => item.data.key,
       {
         from: {height: 0, opacity: 0},
         leave: {height: 0, opacity: 0},
         enter: ({y, height}) => ({y, height, opacity: 1}),
         update: ({y, height}) => ({y, height})
       }
     );*/
    //FIXME: currently not being used, has a few bugs
    return (
        <div style={{height: (rows.length * 59)}}>
            {/*{header && header()}
      {transitions.map(({item, props: {y, ...rest}, key}, index) => (
        <animated.div
          key={key}
          style={{
            zIndex: list.length - index,
            transform: y.interpolate((y: number) => `translate3d(0, ${y}px, 0)`), ...rest
          }}>
          {show(item.data.item, index, index === list.length - 1)}
        </animated.div>
      ))}*/}
        </div>
    )
}