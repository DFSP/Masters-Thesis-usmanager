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
import LocationSelectorMap from "../../../components/map/LocationSelectorMap";
import Dialog from "../../../components/dialogs/Dialog";
import {Point} from "react-simple-maps";

interface Props {
    launchAppCallback: (coordinates: Point) => void;
}

interface State {
    selectedCoordinates?: {label: string, point: Point}
}

export default class LaunchAppDialog extends React.Component<Props, State> {

    constructor(props: Props) {
        super(props);
        this.state = {};
    }

    private onSelectCoordinates = (label: string, coordinates: Point): void => {
        this.setState({selectedCoordinates: {label: label, point: coordinates}});
    }

    private launchAppConfirm = () => {
        if (!this.state.selectedCoordinates) {
            M.toast({html:'<div class="red-text">Error</div><div style="margin-left: 3px"> - location not selected</div>'});
        } else {
            this.props.launchAppCallback(this.state.selectedCoordinates.point);
        }
    }

    public render() {
        const location = this.state.selectedCoordinates ? [this.state.selectedCoordinates] : [];
        return <Dialog id={'launch-app-modal'}
                       title={'Select location'}
                       fullscreen={true}
                       locked={true}
                       confirmCallback={this.launchAppConfirm}>
            <LocationSelectorMap onSelect={this.onSelectCoordinates} locations={location}/>
        </Dialog>;
    }
}