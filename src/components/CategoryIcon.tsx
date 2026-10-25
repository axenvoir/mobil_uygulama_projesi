import React from 'react';
import {
  Grid,
  Milk,
  Drumstick,
  Wheat,
  Coffee,
  Apple,
  CupSoda,
  Snowflake,
  Cookie,
  MoreHorizontal,
  Package,
} from 'lucide-react';

interface CategoryIconProps {
  category: string;
  className?: string;
  size?: number;
}

export const CategoryIcon: React.FC<CategoryIconProps> = ({ category, className = 'w-5 h-5', size = 20 }) => {
  const norm = (category || '').toLowerCase();

  switch (norm) {
    case 'all':
      return <Grid size={size} className={className} />;
    case 'dairy':
      return <Milk size={size} className={className} />;
    case 'meat':
      return <Drumstick size={size} className={className} />;
    case 'grain':
      return <Wheat size={size} className={className} />;
    case 'breakfast':
      return <Coffee size={size} className={className} />;
    case 'veggie':
      return <Apple size={size} className={className} />;
    case 'drink':
      return <CupSoda size={size} className={className} />;
    case 'frozen':
      return <Snowflake size={size} className={className} />;
    case 'snack':
      return <Cookie size={size} className={className} />;
    case 'other':
      return <MoreHorizontal size={size} className={className} />;
    default:
      return <Package size={size} className={className} />;
  }
};
