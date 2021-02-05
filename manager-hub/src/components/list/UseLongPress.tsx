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

import React, {useCallback, useEffect, useState} from 'react';

interface UseLongPressProps {
    callback?: () => void;
    ms: number;
}

const UseLongPress: React.FC<UseLongPressProps> = ({
                                                       children, callback = () => {
    }, ms = 300
                                                   }) => {
    const [startLongPress, setStartLongPress] = useState(false);

    useEffect(() => {
        let timerId: NodeJS.Timeout;
        if (startLongPress) {
            timerId = setTimeout(callback, ms);
        }

        return () => {
            clearTimeout(timerId);
        };
    }, [callback, ms, startLongPress]);

    const start = useCallback(() => {
        setStartLongPress(true);
    }, []);
    const stop = useCallback(() => {
        setStartLongPress(false);
    }, []);

    return (
        <div onMouseDown={start}
             onMouseUp={stop}
             onMouseLeave={stop}
             onTouchStart={start}
             onTouchEnd={stop}>
            {children}
        </div>
    );

};

export default UseLongPress;
