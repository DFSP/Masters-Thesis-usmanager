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


import Card from "../../../components/cards/Card";
import React from "react";
import BaseComponent from "../../../components/BaseComponent";
import {connect} from "react-redux";
import {IRegion} from "./Region";
import CardItem from "../../../components/list/CardItem";

interface State {
    loading: boolean;
}

interface RegionCardProps {
    region: IRegion;
}

interface DispatchToProps {
    /*deleteRegion: (awsRegion: IRegion) => EntitiesAction;*/
}

type Props = DispatchToProps & RegionCardProps;

class RegionCard extends BaseComponent<Props, State> {

    private mounted = false;

    constructor(props: Props) {
        super(props);
        this.state = {
            loading: false
        }
    }

    public componentDidMount(): void {
        this.mounted = true;
    };

    public componentWillUnmount(): void {
        this.mounted = false;
    }

    /*private onDeleteSuccess = (awsRegion: IRegion): void => {
        super.toast(`<span class="green-text">Region <b class="white-text">${awsRegion.name}</b> successfully removed</span>`);
        if (this.mounted) {
            this.setState({loading: false});
        }
        this.props.deleteRegion(awsRegion);
    }*/

    /*private onDeleteFailure = (reason: string, region: IRegion): void => {
        super.toast(`Unable to delete region <a href=/regions/${region.name}><b>${region.name}</b></a>`, 10000, reason, true);
        if (this.mounted) {
            this.setState({loading: false});
        }
    }*/

    public render() {
        const {region} = this.props;
        const {loading} = this.state;
        const CardRegion = Card<IRegion>();
        return <CardRegion id={`region-${region.name}`}
                           title={region.name}
                           link={{to: {pathname: `/regions/${region.name}`, state: region}}}
                           height={'115px'}
                           margin={'10px 0'}
                           hoverable
            /*delete={{
                url: `regions/${awsRegion.name}`,
                successCallback: this.onDeleteSuccess,
                failureCallback: this.onDeleteFailure
            }}*/
                           loading={loading}>
            <CardItem key={'name'}
                      label={'Name'}
                      value={`${region.name}`}/>
            {/*<CardItem key={'active'}
                      label={'Active'}
                      value={`${awsRegion.active}`}/>*/}
            <CardItem key={'coordinates'}
                      label={'Coordinates'}
                      value={`(${region.coordinates.latitude.toFixed(3)}, ${region.coordinates.longitude.toFixed(3)})`}/>
        </CardRegion>
    }

}

const mapDispatchToProps: DispatchToProps = {
    /*deleteRegion*/
};

export default connect(null, mapDispatchToProps)(RegionCard);
