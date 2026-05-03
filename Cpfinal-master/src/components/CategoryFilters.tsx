import React from 'react';

interface CategoryFiltersProps {
  categories: string[];
  activeCategory: string;
  onSelect: (category: string) => void;
}

const CategoryFilters: React.FC<CategoryFiltersProps> = ({
  categories,
  activeCategory,
  onSelect,
}) => (
  <div className="category-pills d-flex flex-wrap gap-2 py-3 mb-4">
    {categories.map((category) => {
      const isActive = activeCategory === category;
      const baseClasses = 'btn btn-sm rounded-pill px-3';
      const variant = isActive ? 'btn-dark shadow-sm' : 'btn-outline-secondary';

      return (
        <button
          key={category}
          type="button"
          className={`${baseClasses} ${variant} transition-all`}
          onClick={() => onSelect(category)}
        >
          {category}
        </button>
      );
    })}
  </div>
);

export default CategoryFilters;

