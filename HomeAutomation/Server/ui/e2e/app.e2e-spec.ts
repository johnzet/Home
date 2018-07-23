import { AngularUiPage } from './app.po';

describe('angular-ui App', function() {
  let page: AngularUiPage;

  beforeEach(() => {
    page = new AngularUiPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
