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

import {ComposableMap, Geographies, Geography, Marker, Point} from "react-simple-maps";
import React, {createRef, memo} from "react";
import * as d3Geo from "d3-geo";

const {geoPath} = d3Geo

type Props = {
    setTooltipContent: (tooltip: string) => void;
    onClick: (label: string, coordinates: Point) => void;
    markers?: {coordinates: Point, marker: JSX.Element}[];
}

type State = {
    scale: number;
}

class MapChart extends React.Component<Props, State> {

    private MAP_MAX_WIDTH = window.innerWidth;
    private MAP_MAX_HEIGHT = window.innerHeight - 125;
    private map = createRef<HTMLDivElement>();

    constructor(props: Props) {
        super(props);
        this.state = {scale: 1.0};
    }

    public componentDidMount() {
        if (global.window) {
            this.handleResize()
            global.window.addEventListener('resize', this.handleResize);
        }
        this.setState({scale: 1.0});
    }

    componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
        this.calculateScale();
    }

    public componentWillUnmount() {
        if (global.window) {
            global.window.removeEventListener('resize', this.handleResize);
        }
    }

    private handleResize = () => {
       this.calculateScale();
    }

    private calculateScale = () => {
        const map = this.map.current;
        if (map) {
            const {width} = map.getBoundingClientRect();
            const newScale = width / this.MAP_MAX_WIDTH;
            if (newScale !== this.state.scale) {
                this.setState({scale: newScale});
            }
        }
    }

    private onGeographyClick = (geography: any, projection: any) => (evt: any) => {
        const gp = geoPath().projection(projection);
        const dim = evt.target.getBoundingClientRect();
        const cx = evt.clientX - dim.left;
        const cy = evt.clientY - dim.top;
        const [orgX, orgY] = gp.bounds(geography)[0];
        const {scale} = this.state;
        const coordinates = projection.invert([orgX + cx / scale, orgY + cy / scale]);
        this.props.onClick(geography.properties.NAME, coordinates);
    }

    public render() {
        const geoUrl = "/resources/world-110m.json";
        const {setTooltipContent, markers} = this.props;
        const divProps = {style: {width: '100%', maxWidth: this.MAP_MAX_WIDTH, margin: '0 auto'}}
        return (
            <div {...divProps} ref={this.map}>
                <ComposableMap data-tip="" projectionConfig={{scale: 315, rotate: [-11, 0, 0]}}
                               width={this.MAP_MAX_WIDTH}
                               height={this.MAP_MAX_HEIGHT}
                               style={{width: '100%', height: 'auto'}}>
                    <Geographies geography={geoUrl} >
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
                    {markers?.map((marker, index) =>
                        <Marker key={index} coordinates={marker.coordinates}>
                            {marker.marker}
                        </Marker>)}
                </ComposableMap>
            </div>
        );
    }

}

export default memo(MapChart);