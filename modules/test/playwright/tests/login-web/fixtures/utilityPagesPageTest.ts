import {test} from '@playwright/test';

import {UtilityPagesPage} from '../pages/UtilityPagesPage';

const utilityPagesPage = test.extend({
    utilityPagesPage: async ({page}, use) => {
        await use(new UtilityPagesPage(page));
    },
});

export {utilityPagesPage};