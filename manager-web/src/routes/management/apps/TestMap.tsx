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

import React, {useState} from "react";
import TooltipedMap from "./TooltipedMap";

type Props = {
}

type State = {
    scale: number;
}

export default class TestMap extends React.Component<Props, State> {

    private MAP_MAX_WIDTH = 1500;
    private MAP_MAX_HEIGHT = 750;

    constructor (props: Props) {
        super(props)
        this.state = {scale: 1.0}
        this.handleResize = this.handleResize.bind(this)
        this.getScale = this.getScale.bind(this)
    }

    componentDidMount () {
        if (global.window) {
            this.handleResize()
            global.window.addEventListener('resize', this.handleResize)
        }
    }

    componentWillUnmount () {
        if (global.window) {
            global.window.removeEventListener('resize', this.handleResize)
        }
    }


    render () {
        const divProps = {style: {width: '100%', maxWidth: this.MAP_MAX_WIDTH, margin: '0 auto'}}
        return (
            <div {...divProps}>
                <TooltipedMap/>
            </div>
        )
    }

    handleResize () {
        const svg = global.document.querySelector('svg');
        if (svg) {
            const {width} = svg.getBoundingClientRect();
            this.setState({scale: width / this.MAP_MAX_WIDTH});
        }
    }

    getScale () {
        return this.state.scale
    }

}