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

import BaseComponent from "../../../components/BaseComponent";
import {IRegion} from "./Region";
import MainLayout from "../../../views/mainLayout/MainLayout";
import React from "react";
import AddButton from "../../../components/form/AddButton";
import styles from "./Regions.module.css";
import CardList from "../../../components/list/CardList";
import RegionCard from "./RegionCard";
import {ReduxState} from "../../../reducers";
import {loadRegions} from "../../../actions";
import {connect} from "react-redux";

interface StateToProps {
  isLoading: boolean
  error?: string | null;
  regions: IRegion[];
}

interface DispatchToProps {
  loadRegions: (name?: string) => any;
}

type Props = StateToProps & DispatchToProps;

class Regions extends BaseComponent<Props, {}> {

  public componentDidMount(): void {
    this.props.loadRegions();
  }

  public render() {
    return (
      <MainLayout>
        <AddButton tooltip={{text: 'Add region', position: 'left'}}
                   pathname={'/regions/new_region?new=true'}/>
        <div className={`${styles.container}`}>
          <CardList<IRegion>
            isLoading={this.props.isLoading}
            error={this.props.error}
            emptyMessage={"No regions to display"}
            list={this.props.regions}
            card={this.region}
            predicate={this.predicate}/>
        </div>
      </MainLayout>
    );
  }

  private region = (region: IRegion): JSX.Element =>
    <RegionCard key={region.id} region={region}/>;

  private predicate = (region: IRegion, search: string): boolean =>
    region.name.toLowerCase().includes(search)
    || region.description.toLowerCase().includes(search);

}

const mapStateToProps = (state: ReduxState): StateToProps => (
  {
    isLoading: state.entities.regions.isLoadingRegions,
    error: state.entities.regions.loadRegionsError,
    regions: (state.entities.regions.data && Object.values(state.entities.regions.data)) || [],
  }
);

const mapDispatchToProps: DispatchToProps = {
  loadRegions,
};

export default connect(mapStateToProps, mapDispatchToProps)(Regions);
