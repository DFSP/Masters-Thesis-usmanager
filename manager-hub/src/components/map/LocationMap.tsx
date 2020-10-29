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

import React from "react";
import MapChart from "./MapChart";
import ReactTooltip from "react-tooltip";
import {Point} from "react-simple-maps";
import Marker, {IMarker} from "./Marker";
import Dialog from "../dialogs/Dialog";
import styles from "../../routes/management/landing/Landing.module.css";

export interface ICoordinates {
    label?: string,
    latitude: number,
    longitude: number,
}

interface Props {
    onSelect?: (marker: IMarker) => void;
    onDeselect?: (marker: IMarker) => void;
    locations: IMarker[],
    marker?: { color?: string, size?: number, labeled?: boolean },
    hover?: boolean,
    clickHighlight?: boolean,
    zoomable?: boolean,
    position?: { coordinates: Point, zoom: number },
    center?: boolean;
    resizable?: boolean;
}

interface State {
    tooltip: string;
    markerSize: number;
    center: boolean;
}

export default class LocationMap extends React.Component<Props, State> {

    private DEFAULT_MARKER_SIZE = 4;

    constructor(props: Props) {
        super(props);
        this.state = {
            tooltip: "",
            markerSize: this.props.marker?.size || this.DEFAULT_MARKER_SIZE,
            center: this.props.center || true
        }
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any) {
        if (prevProps.zoomable !== this.props.zoomable) {
            this.resizeMarkers({coordinates: [0, 0], zoom: 1});
        }
    }

    private setTooltip = (tooltip: string) =>
        this.setState({tooltip});

    private resizeMarkers = (position: { coordinates: Point, zoom: number }) =>
        this.setState({markerSize: (this.props.marker?.size || this.DEFAULT_MARKER_SIZE) / position.zoom})

    private handleCenter = () =>
        this.setState({center: !this.center()})

    private center = () =>
        this.props.center === undefined ? this.state.center : this.props.center && this.state.center;

    public render() {
        const {onSelect, onDeselect, locations, marker, hover, clickHighlight, zoomable, position, resizable} = this.props;
        const {tooltip, markerSize} = this.state;
        const markers = locations.map((location, key): { coordinates: Point, marker: JSX.Element } => ({
            coordinates: [location.longitude, location.latitude],
            marker: <Marker key={key} setTooltipContent={this.setTooltip}
                            title={location.title} label={marker?.labeled ? location.label : undefined}
                            titleCoordinates={location.titleCoordinates === undefined ? true : location.titleCoordinates}
                            location={[location.longitude, location.latitude]}
                            color={location.color || marker?.color || "#2196F3"} size={markerSize}
                            onRemove={() => {
                                this.setTooltip("");
                                onDeselect?.({
                                    title: location.title,
                                    label: location.label,
                                    latitude: location.latitude,
                                    longitude: location.longitude
                                });
                            }}/>
        }));
        const centerButton =
            <button className={`btn-floating btn-flat right tooltipped ${styles.centerButton}`}
                    data-position={'bottom'}
                    data-tooltip={'Center'}
                    onClick={this.handleCenter}
                    type='button'>
                <i className="material-icons">center_focus_weak</i>;
            </button>;
        const map = <>
            <MapChart setTooltipContent={this.setTooltip} onClick={onSelect} markers={markers} hover={hover}
                      clickHighlight={clickHighlight} zoomable={zoomable} position={position} center={this.center()}
                      onZoom={this.resizeMarkers}/>
            <ReactTooltip html multiline>
                {tooltip}
            </ReactTooltip>
        </>;
        return <>
            {resizable &&
            <>
                <button className={`modal-trigger btn-floating btn-flat right tooltipped`}
                        data-position={'bottom'}
                        data-tooltip={'Fullscreen'}
                        data-target={'fullscreen-modal'}
                        type='button'>
                    <i className="material-icons">fullscreen</i>
                </button>
                {centerButton}
                <Dialog id={'fullscreen-modal'}
                        title={'Location'}
                        fullscreen
                        locked
                        footer={false}
                        titleButtons={centerButton}>
                    {map}
                </Dialog>
            </>}
            {map}
        </>;
    }
}