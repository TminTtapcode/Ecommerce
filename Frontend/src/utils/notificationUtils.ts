export const getNotificationRoute = (type: string, referenceId?: string | null): string | null => {
  if (!referenceId) return null;

  switch (type) {
    case 'ORDER_CONFIRMED':
    case 'ORDER_SHIPPED':
    case 'ORDER_DELIVERED':

      return `/orders`;

    case 'NEW_ORDER':

      return `/vendor/orders`;

    case 'NEW_REVIEW':

      return `/products/${referenceId}`;

    default:
      return null;
  }
};
