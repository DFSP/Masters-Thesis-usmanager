import IDatabaseData from "../../../components/IDatabaseData";
import BaseComponent from "../../../components/BaseComponent";
import {RouteComponentProps} from "react-router";
import Form, {IFields, requiredAndTrimmed, requiredAndTrimmedAndSizeRestriction} from "../../../components/form/Form";
import Field from "../../../components/form/Field";
import ListLoadingSpinner from "../../../components/list/ListLoadingSpinner";
import {Error} from "../../../components/errors/Error";
import React from "react";
import Tabs, {Tab} from "../../../components/tabs/Tabs";
import MainLayout from "../../../views/mainLayout/MainLayout";
import {ReduxState} from "../../../reducers";
import {addRegion, loadRegions, updateRegion} from "../../../actions";
import {connect} from "react-redux";
import {IReply} from "../../../utils/api";
import {isNew} from "../../../utils/router";
import {normalize} from "normalizr";
import {Schemas} from "../../../middleware/api";

export interface IRegion extends IDatabaseData {
  name: string;
  description: string;
  active: boolean;
}

const buildNewRegion = (): Partial<IRegion> => ({
  name: undefined,
  description: undefined,
  active: true,
});

interface StateToProps {
  isLoading: boolean;
  error?: string | null;
  region: Partial<IRegion>;
  formRegion?: Partial<IRegion>,
}

interface DispatchToProps {
  loadRegions: (name: string) => void;
  addRegion: (region: IRegion) => void;
  updateRegion: (previousRegion: IRegion, currentRegion: IRegion) => void;
}

interface MatchParams {
  name: string;
}

type Props = StateToProps & DispatchToProps & RouteComponentProps<MatchParams>;

interface State {
  region?: IRegion,
  formRegion?: IRegion,
}

class Region extends BaseComponent<Props, State> {

  state: State = {};
  private mounted = false;

  public componentDidMount(): void {
    this.loadRegion();
    this.mounted = true;
  };

  componentWillUnmount(): void {
    this.mounted = false;
  }

  public render() {
    return (
      <MainLayout>
        <div className="container">
          <Tabs {...this.props} tabs={this.tabs()}/>
        </div>
      </MainLayout>
    );
  }

  private loadRegion = () => {
    if (!isNew(this.props.location.search)) {
      const regionName = this.props.match.params.name;
      this.props.loadRegions(regionName);
    }
  };

  private getRegion = () =>
    this.state.region || this.props.region;

  private getFormRegion = () =>
    this.state.formRegion || this.props.formRegion;

  private isNew = () =>
    isNew(this.props.location.search);

  private onPostSuccess = (reply: IReply<IRegion>): void => {
    const region = reply.data;
    super.toast(`<span class="green-text">Region ${this.mounted ? `<b class="white-text">${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`} saved</span>`);
    this.props.addRegion(region);
    if (this.mounted) {
      this.updateRegion(region);
      this.props.history.replace(region.name);
    }
  };

  private onPostFailure = (reason: string, region: IRegion): void =>
    super.toast(`Unable to save <b>${region.name}</b> region`, 10000, reason, true);

  private onPutSuccess = (reply: IReply<IRegion>): void => {
    const region = reply.data;
    super.toast(`<span class="green-text">Changes to ${this.mounted ? `<b class="white-text">${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`} region have been saved</span>`);
    const previousRegion = this.getRegion();
    if (previousRegion?.id) {
      this.props.updateRegion(previousRegion as IRegion, region)
    }
    if (this.mounted) {
      this.updateRegion(region);
      this.props.history.replace(region.name);
    }
  };

  private onPutFailure = (reason: string, region: IRegion): void =>
    super.toast(`Unable to update ${this.mounted ? `<b>${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`} region`, 10000, reason, true);

  private onDeleteSuccess = (region: IRegion): void => {
    super.toast(`<span class="green-text">Region <b class="white-text">${region.name}</b> successfully removed</span>`);
    if (this.mounted) {
      this.props.history.push(`/regions`);
    }
  };

  private onDeleteFailure = (reason: string, region: IRegion): void =>
    super.toast(`Unable to delete ${this.mounted ? `<b>${region.name}</b>` : `<a href=/regions/${region.name}><b>${region.name}</b></a>`} region`, 10000, reason, true);

  private updateRegion = (region: IRegion) => {
    region = Object.values(normalize(region, Schemas.REGION).entities.regions || {})[0];
    const formRegion = {...region};
    removeFields(formRegion);
    this.setState({region: region, formRegion: formRegion});
  };

  private getFields = (region: Partial<IRegion>): IFields =>
    Object.keys(region).map(key => {
      return {
        [key]: {
          id: key,
          label: key,
          validation:
            key === 'description'
              ? {rule: requiredAndTrimmedAndSizeRestriction, args: 255}
              : {rule: requiredAndTrimmed}
        }
      };
    }).reduce((fields, field) => {
      for (let key in field) {
        fields[key] = field[key];
      }
      return fields;
    }, {});

  private region = () => {
    const {isLoading, error} = this.props;
    const region = this.getRegion();
    const formRegion = this.getFormRegion();
    // @ts-ignore
    const regionKey: (keyof IRegion) = formRegion && Object.keys(formRegion)[0];
    const isNewRegion = this.isNew();
    return (
      <>
        {!isNewRegion && isLoading && <ListLoadingSpinner/>}
        {!isNewRegion && !isLoading && error && <Error message={error}/>}
        {(isNewRegion || !isLoading) && (isNewRegion || !error) && formRegion && (
          <Form id={regionKey}
                fields={this.getFields(formRegion)}
                values={region}
                isNew={isNew(this.props.location.search)}
                post={{
                  url: 'regions',
                  successCallback: this.onPostSuccess,
                  failureCallback: this.onPostFailure
                }}
                put={{
                  url: `regions/${region.name}`,
                  successCallback: this.onPutSuccess,
                  failureCallback: this.onPutFailure
                }}
                delete={{
                  url: `regions/${region.name}`,
                  successCallback: this.onDeleteSuccess,
                  failureCallback: this.onDeleteFailure
                }}>
            {Object.keys(formRegion).map((key, index) =>
              key === 'active'
                ? <Field<boolean> key={index}
                                  id={key}
                                  label={key}
                                  type="dropdown"
                                  dropdown={{
                                    defaultValue: "Is region active?",
                                    values: [true, false]
                                  }}/>
                : key === 'description'
                ? <Field key={index}
                         id={key}
                         label={key}
                         type={'multilinetext'}/>
                : <Field key={index}
                         id={key}
                         label={key}/>
            )}
          </Form>
        )}
      </>
    )
  };

  private tabs = (): Tab[] => [
    {
      title: 'Region',
      id: 'region',
      content: () => this.region()
    },
  ];

}

function removeFields(region: Partial<IRegion>) {
  delete region["id"];
}

function mapStateToProps(state: ReduxState, props: Props): StateToProps {
  const isLoading = state.entities.regions.isLoadingRegions;
  const error = state.entities.regions.loadRegionsError;
  const name = props.match.params.name;
  const region = isNew(props.location.search) ? buildNewRegion() : state.entities.regions.data[name];
  let formRegion;
  if (region) {
    formRegion = {...region};
    removeFields(formRegion);
  }
  return {
    isLoading,
    error,
    region,
    formRegion,
  }
}

const mapDispatchToProps: DispatchToProps = {
  loadRegions,
  addRegion,
  updateRegion,
};

export default connect(mapStateToProps, mapDispatchToProps)(Region);