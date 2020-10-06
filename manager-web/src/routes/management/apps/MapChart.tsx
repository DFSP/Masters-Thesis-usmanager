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

import {ComposableMap, Geographies, Geography, Point, Position, ZoomableGroup} from "react-simple-maps";
import React, {createRef, memo} from "react";
import * as d3Geo from "d3-geo";
import { ContextMenu, MenuItem, ContextMenuTrigger } from "react-contextmenu";

const { geoPath } = d3Geo

type Props = {
    setTooltipContent: (tooltip: string) => void;
}

type State = {
    scale: number;
    center: Point;
}

class MapChart extends React.Component<Props, State> {

    private MAP_MAX_WIDTH = 1500;
    private MAP_MAX_HEIGHT = 750;

    constructor (props: Props) {
        super(props)
        this.state = {scale: 1.0, center: [0, 0]}
        this.handleResize = this.handleResize.bind(this)
        this.getScale = this.getScale.bind(this)
    }

    public componentDidMount() {
        if (global.window) {
            this.handleResize()
            global.window.addEventListener('resize', this.handleResize)
        }
    }

    public componentWillUnmount() {
        if (global.window) {
            global.window.removeEventListener('resize', this.handleResize)
        }
    }

    private handleResize = () => {
        const svg = global.document.querySelector('svg');
        if (svg) {
            const {width} = svg.getBoundingClientRect();
            this.setState({scale: width / this.MAP_MAX_WIDTH});
        }
    }

    private getScale = () => {
        return this.state.scale;
    }

    private launchApp = (e: any, data: any) => {
        console.log(data.foo);
    }

    public render() {
        const geoUrl = "https://raw.githubusercontent.com/zcreativelabs/react-simple-maps/master/topojson-maps/world-110m.json";
        const {setTooltipContent} = this.props;
        return (
            <div>
                <ContextMenuTrigger id="mapContextMenu">
                    <ComposableMap data-tip="">
                        <ZoomableGroup zoom={1.15}>
                            <Geographies geography={geoUrl}>
                                {({ geographies }) =>
                                    geographies.map(geo => (
                                        <Geography
                                            key={geo.rsmKey}
                                            geography={geo}
                                            onClick={() => this.onGeographyClick(this.getScale, geo.projection, () => console.log("test"))}
                                            onMouseEnter={() => {
                                                const { NAME } = geo.properties;
                                                setTooltipContent(`${NAME}`);
                                            }}
                                            onMouseLeave={() => {
                                                setTooltipContent("");
                                            }}
                                            style={{
                                                default: {
                                                    fill: "#D6D6DA",
                                                    outline: "none"
                                                },
                                                hover: {
                                                    fill: "#F53",
                                                    outline: "none"
                                                },
                                                pressed: {
                                                    fill: "#E42",
                                                    outline: "none"
                                                }
                                            }}
                                        />
                                    ))
                                }
                            </Geographies>
                        </ZoomableGroup>
                    </ComposableMap>
                </ContextMenuTrigger>
                <ContextMenu id="mapContextMenu" className="custom-context-menu">
                    <MenuItem className="custom-context-menu-item" data={{foo: 'bar'}} onClick={this.launchApp}>
                        Launch app
                    </MenuItem>
                    <MenuItem className="custom-context-menu-item-divider"/>
                    <MenuItem className="custom-context-menu-item red-text">
                        Close
                    </MenuItem>
                </ContextMenu>
            </div>
        );
    }

    onGeographyClick (scale: any, projection: any, onCoordinatesClick: any) {
        const gp = geoPath().projection(projection)

        return function (geo: any, evt: any) {
            const dim = evt.target.getBoundingClientRect()
            const cx = evt.clientX - dim.left
            const cy = evt.clientY - dim.top
            const [orgX, orgY] = gp.bounds(geo)[0]

            onCoordinatesClick(projection.invert([orgX + cx / scale(), orgY + cy / scale()]))
        }
    }
}

export default memo(MapChart);