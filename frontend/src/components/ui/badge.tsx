import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-[11px] font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-zinc-950 focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-zinc-900 bg-zinc-900 text-white shadow-2xs hover:bg-zinc-800",
        blue:
          "border-blue-600 bg-blue-600 text-white shadow-2xs hover:bg-blue-700",
        secondary:
          "border-zinc-200/90 bg-zinc-100 text-zinc-800 hover:bg-zinc-200/80",
        destructive:
          "border-red-200 bg-red-50 text-red-700 hover:bg-red-100",
        success:
          "border-emerald-200/90 bg-emerald-50 text-emerald-700 hover:bg-emerald-100/80",
        warning:
          "border-amber-200/90 bg-amber-50 text-amber-800 hover:bg-amber-100/80",
        outline:
          "text-zinc-700 border-zinc-200 bg-white",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  );
}

export { Badge, badgeVariants };
