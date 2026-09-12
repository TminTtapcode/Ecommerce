export const getNotificationRoute = (type: string, referenceId?: string | null): string | null => {
  if (!referenceId) return null;

  switch (type) {
    case 'ORDER_CONFIRMED':
    case 'ORDER_SHIPPED':
    case 'ORDER_DELIVERED':
      // Buyer notifications
      return `/orders`; // Or specific order page if available: `/orders/${referenceId}`
    
    case 'NEW_ORDER':
      // Vendor notifications
      return `/vendor/orders`; // The vendor order management page
      
    case 'NEW_REVIEW':
      // Could point to product page or a future review management page
      return `/products/${referenceId}`;
      
    default:
      return null;
  }
};
