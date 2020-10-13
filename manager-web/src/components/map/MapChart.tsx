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

import {ComposableMap, Geographies, Geography, ZoomableGroup} from "react-simple-maps";
import React, {createRef, memo} from "react";
import * as d3Geo from "d3-geo";
import {ICoordinates} from "./LocationSelectorMap";

const {geoPath} = d3Geo

type Props = {
    setTooltipContent: (tooltip: string) => void;
    onClick: (coordinates: ICoordinates) => void;
}

type State = {
    scale: number;
}

class MapChart extends React.Component<Props, State> {

    private MAP_MAX_WIDTH = 1500;
    private MAP_MAX_HEIGHT = 1000;
    private map = createRef<HTMLDivElement>();

    public componentDidMount() {
        if (global.window) {
            this.handleResize()
            global.window.addEventListener('resize', this.handleResize);
        }
        this.setState({scale: 1.0});
    }

    public componentWillUnmount() {
        if (global.window) {
            global.window.removeEventListener('resize', this.handleResize);
        }
    }

    private readonly handleResize = () => {
        const map = this.map.current;
        if (map) {
            const {width} = map.getBoundingClientRect();
            this.setState({scale: width / this.MAP_MAX_WIDTH});
        }
    }

    private onGeographyClick = (geography: any, projection: any) => (evt: any) => {
        console.log(this.state.scale)
        const gp = geoPath().projection(projection);
        const dim = evt.target.getBoundingClientRect();
        const cx = evt.clientX - dim.left;
        const cy = evt.clientY - dim.top;
        const [orgX, orgY] = gp.bounds(geography)[0];
        const {scale} = this.state;
        const coordinates = projection.invert([orgX + cx / scale, orgY + cy / scale]);
        const longitude = coordinates[0];
        const latitude = coordinates[1];
        this.props.onClick({longitude, latitude});
    }

    public render() {
        const geoUrl = "https://raw.githubusercontent.com/zcreativelabs/react-simple-maps/master/topojson-maps/world-110m.json";
        const {setTooltipContent} = this.props;
        return (
            <div style={{width: '100%', maxWidth: this.MAP_MAX_WIDTH, margin: '0 auto'}} ref={this.map}>
                <ComposableMap data-tip=""
                               width={this.MAP_MAX_WIDTH}
                               height={this.MAP_MAX_HEIGHT}
                               style={{width: '100%', height: 'auto'}}>
                    <Geographies geography={geoUrl}>
                        {({geographies, projection}) =>
                            geographies.map(geo => (
                                <Geography
                                    key={geo.rsmKey}
                                    geography={geo}
                                    onClick={this.onGeographyClick(geo, projection)}
                                    onMouseEnter={() => {
                                        const {NAME} = geo.properties;
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
                </ComposableMap>
            </div>
        );
    }

}

export default memo(MapChart);